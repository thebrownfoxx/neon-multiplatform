package com.thebrownfoxx.neon.client.repository

import com.thebrownfoxx.neon.client.model.LocalConversationPreviews
import com.thebrownfoxx.neon.client.model.LocalMessage
import com.thebrownfoxx.neon.common.data.DataOperationError
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.type.id.MessageId
import com.thebrownfoxx.outcome.Outcome
import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    val conversationPreviews: Flow<Outcome<LocalConversationPreviews, DataOperationError>>
    fun get(id: MessageId): Flow<Outcome<LocalMessage, GetError>>
}