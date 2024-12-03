package com.thebrownfoxx.neon.server.repository.exposed

import com.thebrownfoxx.neon.common.data.AddError
import com.thebrownfoxx.neon.common.data.ConnectionError
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.data.UpdateError
import com.thebrownfoxx.neon.common.data.exposed.ExposedDataSource
import com.thebrownfoxx.neon.common.data.exposed.dataTransaction
import com.thebrownfoxx.neon.common.data.exposed.firstOrNotFound
import com.thebrownfoxx.neon.common.data.exposed.mapAddTransaction
import com.thebrownfoxx.neon.common.data.exposed.mapGetTransaction
import com.thebrownfoxx.neon.common.data.exposed.mapUpdateTransaction
import com.thebrownfoxx.neon.common.data.exposed.toCommonUuid
import com.thebrownfoxx.neon.common.data.exposed.toJavaUuid
import com.thebrownfoxx.neon.common.data.exposed.tryAdd
import com.thebrownfoxx.neon.common.data.exposed.tryUpdate
import com.thebrownfoxx.neon.common.data.transaction.ReversibleUnitOutcome
import com.thebrownfoxx.neon.common.data.transaction.asReversible
import com.thebrownfoxx.neon.common.outcome.Failure
import com.thebrownfoxx.neon.common.outcome.Outcome
import com.thebrownfoxx.neon.common.outcome.getOrElse
import com.thebrownfoxx.neon.common.outcome.map
import com.thebrownfoxx.neon.common.outcome.mapError
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.common.type.id.MessageId
import com.thebrownfoxx.neon.server.model.Delivery
import com.thebrownfoxx.neon.server.model.Message
import com.thebrownfoxx.neon.server.repository.MessageRepository
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.alias
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import org.jetbrains.exposed.sql.max
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

class ExposedMessageRepository(
    database: Database,
) : MessageRepository, ExposedDataSource(database, MessageTable) {
    private val reactiveConversationsCache = ReactiveCache(::getConversations)
    private val reactiveMessageCache = ReactiveCache(::get)
    private val reactiveConversationPreviewCache = ReactiveCache(::getConversationPreview)

    override fun getAsFlow(id: MessageId) = reactiveMessageCache.getAsFlow(id)

    override fun getConversationPreviewAsFlow(id: GroupId) =
        reactiveConversationPreviewCache.getAsFlow(id)

    override suspend fun get(id: MessageId): Outcome<Message, GetError> {
        return dataTransaction {
            MessageTable
                .selectAll()
                .where(MessageTable.id eq id.toJavaUuid())
                .firstOrNotFound()
                .map { it.toMessage() }
        }.mapGetTransaction()
    }

    override suspend fun add(message: Message): ReversibleUnitOutcome<AddError> {
        return dataTransaction {
            MessageTable.tryAdd {
                it[id] = message.id.toJavaUuid()
                it[groupId] = message.groupId.toJavaUuid()
                it[senderId] = message.senderId.toJavaUuid()
                it[content] = message.content
                it[timestamp] = message.timestamp
                it[delivery] = message.delivery.name
            }
        }
            .mapAddTransaction()
            .asReversible {
                dataTransaction { MessageTable.deleteWhere { id eq message.id.toJavaUuid() } }
            }
    }

    override suspend fun update(message: Message): ReversibleUnitOutcome<UpdateError> {
        val oldMessage = get(message.id)
            .getOrElse { return Failure(UpdateError.NotFound).asReversible() }

        return dataTransaction {
            MessageTable.tryUpdate {
                it[id] = message.id.toJavaUuid()
                it[groupId] = message.groupId.toJavaUuid()
                it[senderId] = message.senderId.toJavaUuid()
                it[content] = message.content
                it[timestamp] = message.timestamp
                it[delivery] = message.delivery.name
            }
        }
            .mapUpdateTransaction()
            .asReversible {
                dataTransaction {
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

    override suspend fun getConversationsAsFlow(memberId: MemberId) =
        reactiveConversationsCache.getAsFlow(memberId)

    private suspend fun getConversations(
        memberId: MemberId,
    ): Outcome<Set<GroupId>, ConnectionError> {
        return dataTransaction {
            val groupId = MessageTable.groupId.alias("group_id")
            val maxTimestamp = MessageTable.timestamp.max().alias("max_timestamp")

            val conversations = MessageTable
                .join(
                    GroupMemberTable,
                    JoinType.INNER,
                    onColumn = MessageTable.groupId,
                    otherColumn = GroupMemberTable.groupId,
                )
                .select(groupId, maxTimestamp)
                .groupBy(MessageTable.groupId)

            val inConversation = GroupMemberTable.memberId eq memberId.toJavaUuid()

            val messages = conversations
                .where(inConversation)
                .map { GroupId(it[groupId].toCommonUuid()) }
                .toSet()

            println(messages)

            messages
        }.mapError { ConnectionError }
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
        return dataTransaction {
            MessageTable
                .selectAll()
                .where(MessageTable.groupId eq id.toJavaUuid())
                .orderBy(MessageTable.timestamp to SortOrder.DESC)
                .firstOrNull()
                ?.let { MessageId(it[MessageTable.id].toCommonUuid()) }
        }.mapError { ConnectionError }
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