package com.thebrownfoxx.neon.client.repository.local

import com.thebrownfoxx.neon.client.model.LocalConversationPreviews
import com.thebrownfoxx.neon.client.model.LocalMessage
import com.thebrownfoxx.neon.common.data.DataOperationError
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MessageId
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.UnitOutcome
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

interface LocalMessageDataSource {
    val conversationPreviews: Flow<Outcome<LocalConversationPreviews, DataOperationError>>
    fun getMessagesAsFlow(id: GroupId): Flow<Outcome<Set<MessageId>, DataOperationError>>
    fun getMessageAsFlow(id: MessageId): Flow<Outcome<LocalMessage, GetError>>
    fun getOutgoingMessagesAsFlow(): Flow<Outcome<List<LocalMessage>, DataOperationError>>
    suspend fun upsert(message: LocalMessage): UnitOutcome<DataOperationError>
    suspend fun batchUpsert(messages: List<LocalMessage>): UnitOutcome<DataOperationError>
    suspend fun batchUpsert(
        messageIds: Set<LocalTimestampedMessageId>,
    ): UnitOutcome<DataOperationError>

    data class LocalTimestampedMessageId(
        val id: MessageId,
        val groupId: GroupId,
        val timestamp: Instant,
    )
}