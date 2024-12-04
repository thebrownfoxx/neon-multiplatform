package com.thebrownfoxx.neon.client.repository.local.exposed

import com.thebrownfoxx.neon.client.model.LocalConversations
import com.thebrownfoxx.neon.client.model.LocalDelivery
import com.thebrownfoxx.neon.client.model.LocalMessage
import com.thebrownfoxx.neon.client.repository.local.LocalMessageDataSource
import com.thebrownfoxx.neon.common.data.ConnectionError
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.data.exposed.ExposedDataSource
import com.thebrownfoxx.neon.common.data.exposed.dataTransaction
import com.thebrownfoxx.neon.common.data.exposed.firstOrNotFound
import com.thebrownfoxx.neon.common.data.exposed.mapGetTransaction
import com.thebrownfoxx.neon.common.data.exposed.toCommonUuid
import com.thebrownfoxx.neon.common.data.exposed.toJavaUuid
import com.thebrownfoxx.neon.common.outcome.Failure
import com.thebrownfoxx.neon.common.outcome.Outcome
import com.thebrownfoxx.neon.common.outcome.UnitOutcome
import com.thebrownfoxx.neon.common.outcome.map
import com.thebrownfoxx.neon.common.outcome.mapError
import com.thebrownfoxx.neon.common.outcome.onFailure
import com.thebrownfoxx.neon.common.outcome.unitSuccess
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.common.type.id.MessageId
import kotlinx.coroutines.launch
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.alias
import org.jetbrains.exposed.sql.batchUpsert
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import org.jetbrains.exposed.sql.not
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.upsert

class ExposedLocalMessageDataSource(
    private val memberId: MemberId,
    database: Database,
) : LocalMessageDataSource, ExposedDataSource(database, LocalMessageTable) {
    private val reactiveConversationsCache = SingleReactiveCache(::getConversations)
    private val reactiveConversationPreviewCache = ReactiveCache(::getConversationPreview)
    private val reactiveMessageCache = ReactiveCache(::get)

    override val conversations = reactiveConversationsCache.getAsFlow()

    override fun getConversationPreviewAsFlow(groupId: GroupId) =
        reactiveConversationPreviewCache.getAsFlow(groupId)

    override fun getMessageAsFlow(id: MessageId) =
        reactiveMessageCache.getAsFlow(id)

    override suspend fun upsert(message: LocalMessage): UnitOutcome<ConnectionError> {
        dataTransaction {
            LocalMessageTable.upsert {
                it[id] = message.id.toJavaUuid()
                it[groupId] = message.groupId.toJavaUuid()
                it[senderId] = message.senderId.toJavaUuid()
                it[content] = message.content
                it[timestamp] = message.timestamp
                it[delivery] = message.delivery.name
            }
        }.onFailure { return Failure(ConnectionError) }
        reactiveConversationsCache.updateCache()
        reactiveConversationPreviewCache.updateCache(message.groupId)
        reactiveMessageCache.updateCache(message.id)
        return unitSuccess()
    }

    override suspend fun batchUpsert(messages: List<LocalMessage>): UnitOutcome<ConnectionError> {
        dataTransaction {
            LocalMessageTable.batchUpsert(messages) { message ->
                this[LocalMessageTable.id] = message.id.toJavaUuid()
                this[LocalMessageTable.groupId] = message.groupId.toJavaUuid()
                this[LocalMessageTable.senderId] = message.senderId.toJavaUuid()
                this[LocalMessageTable.content] = message.content
                this[LocalMessageTable.timestamp] = message.timestamp
                this[LocalMessageTable.delivery] = message.delivery.name

                dataSourceScope.launch {
                    reactiveConversationPreviewCache.updateCache(message.groupId)
                    reactiveMessageCache.updateCache(message.id)
                }
            }
        }.onFailure { return Failure(ConnectionError) }
        reactiveConversationsCache.updateCache()
        return unitSuccess()
    }

    private suspend fun getConversations(): Outcome<LocalConversations, ConnectionError> {
        return dataTransaction {
            val groupId = LocalMessageTable.groupId.alias("group_id")

            val conversations = LocalMessageTable
                .select(groupId)
                .groupBy(LocalMessageTable.groupId)

            val sent = LocalMessageTable.senderId eq memberId.toJavaUuid()
            val conversationRead = LocalMessageTable.delivery eq LocalDelivery.Read.name or sent

            val unreadConversations = conversations
                .where(not(conversationRead))
                .map { GroupId(it[groupId].toCommonUuid()) }

            val readConversations = conversations
                .where(conversationRead)
                .map { GroupId(it[groupId].toCommonUuid()) }

            val nudgedConversations = when {
                unreadConversations.size > 10 -> unreadConversations.takeLast(2).toSet()
                else -> emptySet()
            }

            LocalConversations(
                nudged = nudgedConversations,
                unread = unreadConversations.toSet(),
                read = readConversations.toSet(),
            )
        }.mapError { ConnectionError }
    }

    private suspend fun getConversationPreview(id: GroupId): Outcome<MessageId?, ConnectionError> {
        return dataTransaction {
            LocalMessageTable
                .selectAll()
                .where(LocalMessageTable.groupId eq id.toJavaUuid())
                .orderBy(LocalMessageTable.timestamp to SortOrder.DESC)
                .firstOrNull()
                ?.let { MessageId(it[LocalMessageTable.id].toCommonUuid()) }
        }.mapError { ConnectionError }
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
}