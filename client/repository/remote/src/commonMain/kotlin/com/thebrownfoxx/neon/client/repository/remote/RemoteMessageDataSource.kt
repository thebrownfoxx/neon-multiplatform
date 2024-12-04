package com.thebrownfoxx.neon.client.repository.remote

import com.thebrownfoxx.neon.common.outcome.Outcome
import com.thebrownfoxx.neon.common.type.id.MessageId
import com.thebrownfoxx.neon.server.model.Message
import kotlinx.coroutines.flow.Flow

interface RemoteMessageDataSource {
    val conversationPreviews: Flow<Outcome<List<Message>, GetConversationPreviewsError>>
    fun getMessageAsFlow(id: MessageId): Flow<Outcome<Message, GetMessageError>>
}

enum class GetConversationPreviewsError {
    ServerError,
}

enum class GetMessageError {
    NotFound,
    ServerError,
}