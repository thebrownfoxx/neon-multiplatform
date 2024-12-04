package com.thebrownfoxx.neon.client.repository

import com.thebrownfoxx.neon.client.model.LocalConversationPreviews
import com.thebrownfoxx.neon.client.model.LocalMessage
import com.thebrownfoxx.neon.common.data.ConnectionError
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.outcome.Outcome
import com.thebrownfoxx.neon.common.type.id.MessageId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow

interface MessageRepository {
    val conversationPreviews: SharedFlow<Outcome<LocalConversationPreviews, ConnectionError>>
    fun get(id: MessageId): Flow<Outcome<LocalMessage, GetError>>
}