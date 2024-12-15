package com.thebrownfoxx.neon.client.service

import com.thebrownfoxx.neon.client.model.LocalConversationPreviews
import com.thebrownfoxx.neon.client.model.LocalMessage
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MessageId
import com.thebrownfoxx.outcome.Outcome
import kotlinx.coroutines.flow.Flow

interface Messenger {
    val conversationPreviews:
            Flow<Outcome<LocalConversationPreviews, ConversationPreviewsUnexpectedError>>

    fun getMessages(groupId: GroupId): Flow<Outcome<Set<MessageId>, GetMessagesError>>

    fun getMessage(id: MessageId): Flow<Outcome<LocalMessage, GetMessageError>>

    data object ConversationPreviewsUnexpectedError

    enum class GetMessagesError {
        Idk,
    }

    enum class GetMessageError {
        NotFound,
        UnexpectedError,
    }
}