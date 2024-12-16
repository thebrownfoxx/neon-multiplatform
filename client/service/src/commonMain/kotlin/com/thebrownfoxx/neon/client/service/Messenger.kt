package com.thebrownfoxx.neon.client.service

import com.thebrownfoxx.neon.client.model.LocalConversationPreviews
import com.thebrownfoxx.neon.client.model.LocalMessage
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MessageId
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.UnitOutcome
import kotlinx.coroutines.flow.Flow

interface Messenger {
    val conversationPreviews:
            Flow<Outcome<LocalConversationPreviews, ConversationPreviewsUnexpectedError>>

    fun getMessages(groupId: GroupId): Flow<Outcome<Set<MessageId>, GetMessagesError>>

    fun getMessage(id: MessageId): Flow<Outcome<LocalMessage, GetMessageError>>

    suspend fun sendMessage(groupId: GroupId, content: String): UnitOutcome<Unit>

    data object ConversationPreviewsUnexpectedError

    enum class GetMessagesError {
        Idk,
    }

    enum class GetMessageError {
        NotFound,
        UnexpectedError,
    }
}