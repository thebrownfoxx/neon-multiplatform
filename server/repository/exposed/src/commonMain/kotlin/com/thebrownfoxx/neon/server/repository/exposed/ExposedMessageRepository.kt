package com.thebrownfoxx.neon.server.repository.exposed

import com.thebrownfoxx.neon.common.data.AddError
import com.thebrownfoxx.neon.common.data.ConnectionError
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.data.UpdateError
import com.thebrownfoxx.neon.common.data.exposed.ExposedDataSource
import com.thebrownfoxx.neon.common.data.exposed.dbQuery
import com.thebrownfoxx.neon.common.data.exposed.firstOrNotFound
import com.thebrownfoxx.neon.common.data.exposed.toCommonUuid
import com.thebrownfoxx.neon.common.data.exposed.toJavaUuid
import com.thebrownfoxx.neon.common.data.exposed.tryAdd
import com.thebrownfoxx.neon.common.data.exposed.tryUpdate
import com.thebrownfoxx.neon.common.data.transaction.ReversibleUnitOutcome
import com.thebrownfoxx.neon.common.data.transaction.asReversible
import com.thebrownfoxx.neon.common.type.Failure
import com.thebrownfoxx.neon.common.type.Outcome
import com.thebrownfoxx.neon.common.type.Success
import com.thebrownfoxx.neon.common.type.asSuccess
import com.thebrownfoxx.neon.common.type.getOrElse
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.common.type.id.MessageId
import com.thebrownfoxx.neon.common.type.map
import com.thebrownfoxx.neon.server.model.Delivery
import com.thebrownfoxx.neon.server.model.Message
import com.thebrownfoxx.neon.server.repository.CategorizedConversations
import com.thebrownfoxx.neon.server.repository.MessageRepository
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.alias
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import org.jetbrains.exposed.sql.max
import org.jetbrains.exposed.sql.not
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

class ExposedMessageRepository(
    database: Database,
) : MessageRepository, ExposedDataSource(database, MessageTable) {
    private val reactiveMessageCache = ReactiveCache(::get)
    private val reactiveConversationPreviewCache = ReactiveCache(::getConversationPreview)

    override fun getAsFlow(id: MessageId) = reactiveMessageCache.getAsFlow(id)

    override fun getConversationPreviewAsFlow(id: GroupId) =
        reactiveConversationPreviewCache.getAsFlow(id)

    override suspend fun get(id: MessageId): Outcome<Message, GetError> {
        return dbQuery {
            MessageTable
                .selectAll()
                .where(MessageTable.id eq id.toJavaUuid())
                .firstOrNotFound()
                .map { it.toMessage() }
        }
    }

    override suspend fun add(message: Message): ReversibleUnitOutcome<AddError> {
        return tryAdd {
            dbQuery {
                MessageTable.insert {
                    it[id] = message.id.toJavaUuid()
                    it[groupId] = message.groupId.toJavaUuid()
                    it[senderId] = message.senderId.toJavaUuid()
                    it[content] = message.content
                    it[timestamp] = message.timestamp
                    it[delivery] = message.delivery.name
                }
            }
        }.asReversible { dbQuery { MessageTable.deleteWhere { id eq message.id.toJavaUuid() } } }
    }

    override suspend fun update(message: Message): ReversibleUnitOutcome<UpdateError> {
        val oldMessage = get(message.id)
            .getOrElse { return Failure(UpdateError.NotFound).asReversible() }

        return tryUpdate {
            dbQuery {
                MessageTable.update {
                    it[id] = message.id.toJavaUuid()
                    it[groupId] = message.groupId.toJavaUuid()
                    it[senderId] = message.senderId.toJavaUuid()
                    it[content] = message.content
                    it[timestamp] = message.timestamp
                    it[delivery] = message.delivery.name
                }
            }
        }.asReversible {
            dbQuery {
                MessageTable.update {
                    it[id] = oldMessage.id.toJavaUuid()
                    it[groupId] = oldMessage.groupId.toJavaUuid()
                    it[senderId] = oldMessage.senderId.toJavaUuid()
                    it[content] = oldMessage.content
                    it[timestamp] = oldMessage.timestamp
                    it[delivery] = oldMessage.delivery.name
                }
            }
        }
    }

    @Deprecated("Use getConversations(MemberId) instead")
    override suspend fun getConversations(
        memberId: MemberId,
        count: Int,
        offset: Int,
        read: Boolean?,
        descending: Boolean,
    ): Outcome<Set<GroupId>, ConnectionError> {
        error("Use the other getConversations")
    }

    override suspend fun getConversations(
        memberId: MemberId,
    ): Outcome<CategorizedConversations, ConnectionError> {
        return dbQuery {
            val maxTimestamp = MessageTable.timestamp.max().alias("max_timestamp")

            val conversations = MessageTable
                .join(
                    GroupMemberTable,
                    JoinType.INNER,
                    onColumn = MessageTable.groupId,
                    otherColumn = GroupMemberTable.groupId,
                )
                .select(MessageTable.groupId, maxTimestamp)
                .groupBy(MessageTable.groupId)

            val inConversation = GroupMemberTable.memberId eq memberId.toJavaUuid()
            val sent = MessageTable.senderId eq memberId.toJavaUuid()
            val messageRead = MessageTable.delivery eq Delivery.Read.name or sent

            val unreadMessages = conversations
                .where(inConversation and not(messageRead))
                .map { GroupId(it[GroupMemberTable.groupId].toCommonUuid()) }
                .toSet()

            val readMessages = conversations
                .where(inConversation and messageRead)
                .map { GroupId(it[GroupMemberTable.groupId].toCommonUuid()) }
                .toSet()

            Success(CategorizedConversations(unreadMessages, readMessages))
        }
    }

    @Deprecated("Use getConversations instead")
    override suspend fun getConversationCount(
        memberId: MemberId,
        read: Boolean?,
    ): Outcome<Int, ConnectionError> {
        error("Use getConversations instead")
    }

    override suspend fun getMessages(
        groupId: GroupId,
        count: Int,
        offset: Int,
    ): Outcome<Set<MessageId>, ConnectionError> {
        TODO("Not yet implemented")
    }

    override suspend fun getUnreadMessages(groupId: GroupId): Outcome<Set<MessageId>, ConnectionError> {
        TODO("Not yet implemented")
    }

    private suspend fun getConversationPreview(id: GroupId): Outcome<MessageId?, ConnectionError> {
        return dbQuery {
            MessageTable
                .selectAll()
                .where(MessageTable.groupId eq id.toJavaUuid())
                .orderBy(MessageTable.timestamp to SortOrder.DESC)
                .firstOrNull()
                ?.let { MessageId(it.get(MessageTable.id).toCommonUuid()) }
                .asSuccess()
        }
    }

    private fun ResultRow.toMessage() = Message(
        id = MessageId(this[MessageTable.id].toCommonUuid()),
        groupId = GroupId(this[MessageTable.groupId].toCommonUuid()),
        senderId = MemberId(this[MessageTable.senderId].toCommonUuid()),
        content = this[MessageTable.content],
        timestamp = this[MessageTable.timestamp],
        delivery = Delivery.valueOf(this[MessageTable.delivery])
    )
}

private object MessageTable : Table("message") {
    val id = uuid("id")
    val groupId = uuid("group_id")
    val senderId = uuid("sender_id")
    val content = text("content")
    val timestamp = timestamp("timestamp")

    // We could make like a Postgres mapping but it's too complicated and bound to Postgres
    // (at least with Exposed's current implementation)
    // So I'm just gonna save it as a string and parse it later
    val delivery = varchar("delivery", 32)
}