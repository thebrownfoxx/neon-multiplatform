package com.thebrownfoxx.neon.client.repository.local

import com.thebrownfoxx.neon.client.model.LocalConversationPreviews
import com.thebrownfoxx.neon.client.model.LocalMessage
import com.thebrownfoxx.neon.common.data.DataOperationError
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.type.id.MessageId
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.UnitOutcome
import kotlinx.coroutines.flow.Flow

interface LocalMessageDataSource {
    val conversationPreviews: Flow<Outcome<LocalConversationPreviews, DataOperationError>>
    fun getMessageAsFlow(id: MessageId): Flow<Outcome<LocalMessage, GetError>>
    suspend fun upsert(message: LocalMessage): UnitOutcome<DataOperationError>
    suspend fun batchUpsert(messages: List<LocalMessage>): UnitOutcome<DataOperationError>
}