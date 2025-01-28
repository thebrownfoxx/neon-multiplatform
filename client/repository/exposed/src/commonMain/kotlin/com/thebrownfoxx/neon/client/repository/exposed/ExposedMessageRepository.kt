package com.thebrownfoxx.neon.client.repository.exposed

import com.thebrownfoxx.neon.client.model.LocalChatPreviews
import com.thebrownfoxx.neon.client.model.LocalDelivery
import com.thebrownfoxx.neon.client.model.LocalMessage
import com.thebrownfoxx.neon.client.model.LocalTimestampedMessageId
import com.thebrownfoxx.neon.client.repository.MessageRepository
import com.thebrownfoxx.neon.common.data.DataOperationError
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.data.ReactiveCache
import com.thebrownfoxx.neon.common.data.SingleReactiveCache
import com.thebrownfoxx.neon.common.data.exposed.dataTransaction
import com.thebrownfoxx.neon.common.data.exposed.firstOrNotFound
import com.thebrownfoxx.neon.common.data.exposed.initializeExposeDatabase
import com.thebrownfoxx.neon.common.data.exposed.mapGetTransaction
import com.thebrownfoxx.neon.common.data.exposed.mapOperationTransaction
import com.thebrownfoxx.neon.common.data.exposed.mapUnitOperationTransaction
import com.thebrownfoxx.neon.common.data.exposed.toCommonUuid
import com.thebrownfoxx.neon.common.data.exposed.toJavaUuid
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.common.type.id.MessageId
import com.thebrownfoxx.outcome.Failure
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.Success
import com.thebrownfoxx.outcome.UnitOutcome
import com.thebrownfoxx.outcome.map.getOrElse
import com.thebrownfoxx.outcome.map.map
import com.thebrownfoxx.outcome.map.onSuccess
import com.thebrownfoxx.outcome.map.transform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ExpressionAlias
import org.jetbrains.exposed.sql.LiteralOp
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.QueryAlias
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.rowNumber
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.UUIDColumnType
import org.jetbrains.exposed.sql.alias
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.batchUpsert
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import org.jetbrains.exposed.sql.longLiteral
import org.jetbrains.exposed.sql.not
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.stringLiteral
import org.jetbrains.exposed.sql.upsert
import java.util.UUID

class ExposedMessageRepository(
    database: Database,
    private val getMemberId: suspend () -> MemberId, // TODO: This should be bound to authenticator? idk man
    externalScope: CoroutineScope,
) : MessageRepository {
    init {
        initializeExposeDatabase(database, LocalMessageTable)
    }

    private val chatPreviewsCache = SingleReactiveCache(externalScope, ::getChatPreview)
    private val messagesCache = ReactiveCache(externalScope, ::getMessages)
    private val messageCache = ReactiveCache(externalScope, ::get)

    init {
        externalScope.launch {
            val outgoingMessages = getOutgoingMessages().getOrElse { return@launch }
            outgoingMessages.forEach { outgoingQueue.send(it) }
        }
    }

    @Deprecated("Use getChatPreviews instead")
    override val chatPreviews:
            Flow<Outcome<LocalChatPreviews, DataOperationError>> =
        chatPreviewsCache.getAsFlow()

    override fun getChatPreviews(memberId: MemberId): Flow<Outcome<LocalChatPreviews, DataOperationError>> {
        TODO("Not yet implemented")
    }
    @Deprecated("Use getOutgoingQueue instead")
    override val outgoingQueue = Channel<LocalMessage>(Channel.BUFFERED)

    override fun getOutgoingQueue(memberId: MemberId): ReceiveChannel<LocalMessage> {
        TODO("Not yet implemented")
    }

    override fun getMessagesAsFlow(
        id: GroupId,
    ): Flow<Outcome<List<LocalTimestampedMessageId>, DataOperationError>> {
        return messagesCache.getAsFlow(id)
    }

    override fun getMessageAsFlow(id: MessageId): Flow<Outcome<LocalMessage, GetError>> {
        return messageCache.getAsFlow(id)
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
        }
            .mapUnitOperationTransaction()
            .onSuccess {
                messageCache.update(message.id)
                if (message.delivery == LocalDelivery.Sending) {
                    outgoingQueue.send(message)
                    chatPreviewsCache.update()
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
                chatPreviewsCache.update()
            }
    }

    override suspend fun batchUpsertTimestampedIds(
        messageIds: List<LocalTimestampedMessageId>,
    ): UnitOutcome<DataOperationError> {
        return dataTransaction {
            LocalMessageTable.batchUpsert(
                messageIds,
                onUpdateExclude = listOf(
                    LocalMessageTable.senderId,
                    LocalMessageTable.content,
                    LocalMessageTable.delivery,
                ),
            ) { messageId ->
                this[LocalMessageTable.id] = messageId.id.toJavaUuid()
                this[LocalMessageTable.groupId] = messageId.groupId.toJavaUuid()
                this[LocalMessageTable.timestamp] = messageId.timestamp
            }
        }
            .mapUnitOperationTransaction()
            .onSuccess {
                messageIds
                    .map { it.groupId }
                    .toSet()
                    .forEach { messagesCache.update(it) }
            }
    }

    private suspend fun getChatPreview(): Outcome<LocalChatPreviews, DataOperationError> {
        val memberId = getMemberId()
        return dataTransaction {
            val unreadPreviews = getChatPreviews(memberId, read = false)
            val readPreviews = getChatPreviews(memberId, read = true)
            val nudgedPreviews = when {
                unreadPreviews.size > 10 -> unreadPreviews.takeLast(2)
                else -> emptyList()
            }

            LocalChatPreviews(
                nudgedPreviews = nudgedPreviews,
                unreadPreviews = unreadPreviews.filter { unread ->
                    nudgedPreviews.none { it.groupId == unread.groupId }
                },
                readPreviews = readPreviews,
            )
        }.mapOperationTransaction()
    }

    private fun getChatPreviews(
        memberId: MemberId,
        read: Boolean,
    ): List<LocalMessage> {
        val rowNumber = rowNumber()
            .over()
            .partitionBy(LocalMessageTable.groupId)
            .orderBy(LocalMessageTable.timestamp, SortOrder.DESC)
            .alias("row_number")

        val aliases = LocalMessageAliases.generate("ranked")

        val rankedMessages = LocalMessageTable
            .select(aliases.columns + rowNumber)
            .alias("ranked_messages")

        val sent = aliases.senderId.aliasOnlyExpression() eq
                LiteralOp(UUIDColumnType(), memberId.toJavaUuid())

        val conversationRead = aliases.delivery.aliasOnlyExpression() eq
                stringLiteral(LocalDelivery.Read.name) or sent

        val isFirst = rowNumber.aliasOnlyExpression() eq longLiteral(1L)

        val readModifier = when {
            read -> conversationRead
            else -> not(conversationRead)
        }

        return rankedMessages.selectAll(aliases)
            .where(isFirst and readModifier)
            .map { it.toLocalMessage(aliases) }
            .filterNotNull()
    }

    private fun QueryAlias.selectAll(aliases: LocalMessageAliases): Query {
        return with(aliases) {
            select(
                id.aliasOnlyExpression(),
                groupId.aliasOnlyExpression(),
                senderId.aliasOnlyExpression(),
                content.aliasOnlyExpression(),
                timestamp.aliasOnlyExpression(),
                delivery.aliasOnlyExpression(),
            )
        }
    }

    private fun ResultRow.toLocalMessage(aliases: LocalMessageAliases): LocalMessage? {
        val row = this
        return with(aliases) {
            val senderIdValue = row[senderId.aliasOnlyExpression()]
                ?.let { MemberId(it.toCommonUuid()) }
            val contentValue = row[content.aliasOnlyExpression()]
            val deliveryValue = row[delivery.aliasOnlyExpression()]
                ?.let { LocalDelivery.valueOf(it) }
            if (senderIdValue == null || contentValue == null || deliveryValue == null) return null
            LocalMessage(
                id = MessageId(row[id.aliasOnlyExpression()].toCommonUuid()),
                groupId = GroupId(row[groupId.aliasOnlyExpression()].toCommonUuid()),
                senderId = senderIdValue,
                content = contentValue,
                timestamp = row[timestamp.aliasOnlyExpression()],
                delivery = deliveryValue,
            )
        }
    }

    private suspend fun getMessages(
        groupId: GroupId,
    ): Outcome<List<LocalTimestampedMessageId>, DataOperationError> {
        return dataTransaction {
            LocalMessageTable
                .selectAll()
                .orderBy(LocalMessageTable.timestamp to SortOrder.DESC)
                .where(LocalMessageTable.groupId eq groupId.toJavaUuid())
                .map { it.toLocalTimestampedMessageId() }
        }.mapOperationTransaction()
    }

    private suspend fun get(id: MessageId): Outcome<LocalMessage, GetError> {
        return dataTransaction {
            LocalMessageTable
                .selectAll()
                .where(LocalMessageTable.id eq id.toJavaUuid())
                .firstOrNotFound()
                .map { it.toLocalMessage() }
        }
            .mapGetTransaction()
            .transform(
                onSuccess = { it?.let { Success(it) } ?: Failure(GetError.NotFound) },
                onFailure = { Failure(it) },
            )
    }

    private suspend fun getOutgoingMessages(): Outcome<List<LocalMessage>, DataOperationError> {
        return dataTransaction {
            LocalMessageTable
                .selectAll()
                .where(LocalMessageTable.delivery eq LocalDelivery.Sending.name)
                .map { it.toLocalMessage() }
                .filterNotNull()
        }.mapOperationTransaction()
    }

    private fun ResultRow.toLocalTimestampedMessageId() = LocalTimestampedMessageId(
        id = MessageId(this[LocalMessageTable.id].toCommonUuid()),
        groupId = GroupId(this[LocalMessageTable.groupId].toCommonUuid()),
        timestamp = this[LocalMessageTable.timestamp],
    )

    private fun ResultRow.toLocalMessage(): LocalMessage? {
        val senderId = this[LocalMessageTable.senderId]?.let { MemberId(it.toCommonUuid()) }
        val content = this[LocalMessageTable.content]
        val delivery = this[LocalMessageTable.delivery]?.let { LocalDelivery.valueOf(it) }
        if (senderId == null || content == null || delivery == null) return null
        return LocalMessage(
            id = MessageId(this[LocalMessageTable.id].toCommonUuid()),
            groupId = GroupId(this[LocalMessageTable.groupId].toCommonUuid()),
            senderId = senderId,
            content = content,
            timestamp = this[LocalMessageTable.timestamp],
            delivery = delivery,
        )
    }
}

private object LocalMessageTable : Table("message") {
    val id = uuid("id")
    val groupId = uuid("group_id")
    val senderId = uuid("sender_id").nullable()
    val content = text("content").nullable()
    val timestamp = timestamp("timestamp")

    // We could make like a Postgres mapping but it's too complicated and bound to Postgres
    // (at least with Exposed's current implementation)
    // So I'm just gonna save it as a string and parse it later
    val delivery = varchar("delivery", 32).nullable()

    override val primaryKey = PrimaryKey(id)
}

data class LocalMessageAliases(
    val id: ExpressionAlias<UUID>,
    val groupId: ExpressionAlias<UUID>,
    val senderId: ExpressionAlias<UUID?>,
    val content: ExpressionAlias<String?>,
    val timestamp: ExpressionAlias<Instant>,
    val delivery: ExpressionAlias<String?>,
) {
    companion object {
        fun generate(prefix: String) = LocalMessageAliases(
            id = LocalMessageTable.id.alias("${prefix}_id"),
            groupId = LocalMessageTable.groupId.alias("${prefix}_group_id"),
            senderId = LocalMessageTable.senderId.alias("${prefix}_sender_id"),
            content = LocalMessageTable.content.alias("${prefix}_content"),
            timestamp = LocalMessageTable.timestamp.alias("${prefix}_timestamp"),
            delivery = LocalMessageTable.delivery.alias("${prefix}_delivery"),
        )
    }

    val columns = listOf(id, groupId, senderId, content, timestamp, delivery)
}