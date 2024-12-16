package com.thebrownfoxx.neon.client.repository.local.exposed

import com.thebrownfoxx.neon.client.model.LocalConversationPreviews
import com.thebrownfoxx.neon.client.model.LocalDelivery
import com.thebrownfoxx.neon.client.model.LocalMessage
import com.thebrownfoxx.neon.client.repository.local.LocalMessageDataSource
import com.thebrownfoxx.neon.client.repository.local.LocalMessageDataSource.LocalTimestampedMessageId
import com.thebrownfoxx.neon.common.data.DataOperationError
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.data.exposed.ExposedDataSource
import com.thebrownfoxx.neon.common.data.exposed.dataTransaction
import com.thebrownfoxx.neon.common.data.exposed.firstOrNotFound
import com.thebrownfoxx.neon.common.data.exposed.mapGetTransaction
import com.thebrownfoxx.neon.common.data.exposed.mapOperationTransaction
import com.thebrownfoxx.neon.common.data.exposed.mapUnitOperationTransaction
import com.thebrownfoxx.neon.common.data.exposed.toCommonUuid
import com.thebrownfoxx.neon.common.data.exposed.toJavaUuid
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.common.type.id.MessageId
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.UnitOutcome
import com.thebrownfoxx.outcome.map.map
import com.thebrownfoxx.outcome.map.onSuccess
import kotlinx.coroutines.flow.Flow
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.alias
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.batchUpsert
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import org.jetbrains.exposed.sql.max
import org.jetbrains.exposed.sql.not
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.upsert

class ExposedLocalMessageDataSource(
    database: Database,
    private val getMemberId: suspend () -> MemberId, // TODO: This should be bound to authenticator? idk man
) : LocalMessageDataSource,
    ExposedDataSource(database, LocalMessageTable, LocalTimestampedMessageIdTable) {
    private val conversationsCache = SingleReactiveCache(::getConversations) // TODO: remove "reactive" from reactive cache names like this
    private val messagesCache = ReactiveCache(::getMessages)
    private val messageCache = ReactiveCache(::get)
    private val outgoingMessagesCache = SingleReactiveCache(::getOutgoingMessages)

    override val conversationPreviews:
            Flow<Outcome<LocalConversationPreviews, DataOperationError>> =
        conversationsCache.getAsFlow() // TODO: Specify complex return types like this

    override fun getMessagesAsFlow(
        id: GroupId,
    ): Flow<Outcome<Set<MessageId>, DataOperationError>> {
        return messagesCache.getAsFlow(id)
    }

    override fun getMessageAsFlow(id: MessageId): Flow<Outcome<LocalMessage, GetError>> {
        return messageCache.getAsFlow(id)
    }

    override fun getOutgoingMessagesAsFlow(): Flow<Outcome<List<LocalMessage>, DataOperationError>> {
        return outgoingMessagesCache.getAsFlow()
    }

    override suspend fun upsert(message: LocalMessage): UnitOutcome<DataOperationError> {
        return dataTransaction {
            LocalMessageTable.upsert {
                it[id] = message.id.toJavaUuid()
                it[groupId] = message.groupId.toJavaUuid()
                it[senderId] = message.senderId.toJavaUuid()
                it[content] = message.content
                it[timestamp] = message.timestamp
                it[delivery] = message.delivery.name
            }
            LocalTimestampedMessageIdTable.upsert {
                it[id] = message.id.toJavaUuid()
                it[groupId] = message.groupId.toJavaUuid()
                it[timestamp] = message.timestamp
            }
        }
            .mapUnitOperationTransaction()
            .onSuccess {
                messageCache.update(message.id)
                if (message.delivery == LocalDelivery.Sending) {
                    outgoingMessagesCache.update()
                    conversationsCache.update()
                    messagesCache.update(message.groupId)
                }
            }
    }

    override suspend fun batchUpsert(
        messages: List<LocalMessage>,
    ): UnitOutcome<DataOperationError> {
        return dataTransaction {
            LocalMessageTable.batchUpsert(messages) { message ->
                this[LocalMessageTable.id] = message.id.toJavaUuid()
                this[LocalMessageTable.groupId] = message.groupId.toJavaUuid()
                this[LocalMessageTable.senderId] = message.senderId.toJavaUuid()
                this[LocalMessageTable.content] = message.content
                this[LocalMessageTable.timestamp] = message.timestamp
                this[LocalMessageTable.delivery] = message.delivery.name
            }
        }
            .mapUnitOperationTransaction()
            .onSuccess {
                messages.forEach { messageCache.update(it.id) }
                conversationsCache.update()
            }
    }

    override suspend fun batchUpsert(
        messageIds: Set<LocalTimestampedMessageId>,
    ): UnitOutcome<DataOperationError> {
        return dataTransaction {
            LocalTimestampedMessageIdTable.batchUpsert(messageIds) { messageId ->
                this[LocalTimestampedMessageIdTable.id] = messageId.id.toJavaUuid()
                this[LocalTimestampedMessageIdTable.groupId] = messageId.groupId.toJavaUuid()
                this[LocalTimestampedMessageIdTable.timestamp] = messageId.timestamp
            }
        }
            .mapUnitOperationTransaction()
//            .onSuccess {
//                messageIds
//                    .map { it.groupId }
//                    .toSet()
//                    .forEach { messagesCache.update(it) }
//                conversationsCache.update()
//            }
    }

    private suspend fun getConversations(): Outcome<LocalConversationPreviews, DataOperationError> {
        val memberId = getMemberId()
        return dataTransaction {
            val sent = LocalMessageTable.senderId eq memberId.toJavaUuid()
            val conversationRead = LocalMessageTable.delivery eq LocalDelivery.Read.name or sent

            val unreadPreviews = getConversationPreviews()
                .where(not(conversationRead))
                .map { it.toLocalMessage() }

            val readPreviews = getConversationPreviews()
                .where(conversationRead)
                .map { it.toLocalMessage() }

            val nudgedPreviews = when {
                unreadPreviews.size > 10 -> unreadPreviews.takeLast(2)
                else -> emptyList()
            }

            LocalConversationPreviews(
                nudgedPreviews = nudgedPreviews,
                unreadPreviews = unreadPreviews.filter { unread ->
                    nudgedPreviews.none { it.groupId != unread.groupId }
                },
                readPreviews = readPreviews,
            )
        }.mapOperationTransaction()
    }

    private fun getConversationPreviews(): Query {
        val groupId = LocalMessageTable.groupId.alias("group_id")
        val maxTimestamp = LocalMessageTable.timestamp.max().alias("max_timestamp")

        val conversations = LocalMessageTable
            .select(groupId, maxTimestamp)
            .groupBy(LocalMessageTable.groupId)
            .alias("conversations")

        val conversationPreviews = LocalMessageTable
            .join(
                conversations,
                JoinType.INNER,
            ) {
                (LocalMessageTable.groupId eq conversations[groupId]) and
                        (LocalMessageTable.timestamp eq conversations[maxTimestamp])
            }.selectAll()
        return conversationPreviews
    }

    private suspend fun getMessages(groupId: GroupId): Outcome<Set<MessageId>, DataOperationError> {
        return dataTransaction {
            LocalTimestampedMessageIdTable
                .selectAll()
                .orderBy(LocalTimestampedMessageIdTable.timestamp to SortOrder.DESC)
                .where(LocalTimestampedMessageIdTable.groupId eq groupId.toJavaUuid())
                .map { MessageId(it[LocalTimestampedMessageIdTable.id].toCommonUuid()) }
                .toSet()
        }.mapOperationTransaction()
    }

    private suspend fun get(id: MessageId): Outcome<LocalMessage, GetError> {
        return dataTransaction {
            LocalMessageTable
                .selectAll()
                .where(LocalMessageTable.id eq id.toJavaUuid())
                .firstOrNotFound()
                .map { it.toLocalMessage() }
        }.mapGetTransaction()
    }

    private suspend fun getOutgoingMessages(): Outcome<List<LocalMessage>, DataOperationError> {
        return dataTransaction {
            LocalMessageTable
                .selectAll()
                .where(LocalMessageTable.delivery eq LocalDelivery.Sending.name)
                .map { it.toLocalMessage() }
        }.mapOperationTransaction()
    }

    private fun ResultRow.toLocalMessage() = LocalMessage(
        id = MessageId(this[LocalMessageTable.id].toCommonUuid()),
        groupId = GroupId(this[LocalMessageTable.groupId].toCommonUuid()),
        senderId = MemberId(this[LocalMessageTable.senderId].toCommonUuid()),
        content = this[LocalMessageTable.content],
        timestamp = this[LocalMessageTable.timestamp],
        delivery = LocalDelivery.valueOf(this[LocalMessageTable.delivery]),
    )
}

private object LocalMessageTable : Table("local_message") {
    val id = uuid("id")
    val groupId = uuid("group_id")
    val senderId = uuid("sender_id")
    val content = text("content")
    val timestamp = timestamp("timestamp")

    // We could make like a Postgres mapping but it's too complicated and bound to Postgres
    // (at least with Exposed's current implementation)
    // So I'm just gonna save it as a string and parse it later
    val delivery = varchar("delivery", 32)

    override val primaryKey = PrimaryKey(id)
}

private object LocalTimestampedMessageIdTable : Table("local_timestamped_message_id") {
    val id = uuid("id")
    val groupId = uuid("group_id")
    val timestamp = timestamp("timestamp")

    override val primaryKey = PrimaryKey(id)
} // TODO: Merge this with LocalMessageTable and just make things nullable