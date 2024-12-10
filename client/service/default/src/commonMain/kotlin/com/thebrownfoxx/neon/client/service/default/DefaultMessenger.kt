package com.thebrownfoxx.neon.client.service.default

import com.thebrownfoxx.neon.client.model.LocalMessage
import com.thebrownfoxx.neon.client.repository.MessageRepository
import com.thebrownfoxx.neon.client.service.Messenger
import com.thebrownfoxx.neon.client.service.Messenger.ConversationPreviewsUnexpectedError
import com.thebrownfoxx.neon.client.service.Messenger.GetMessageError
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.type.id.MessageId
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.mapError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DefaultMessenger(private val messageRepository: MessageRepository) : Messenger {
    override val conversationPreviews = messageRepository.conversationPreviews.map { outcome ->
        outcome.mapError { ConversationPreviewsUnexpectedError }
    }

    override fun getMessage(id: MessageId): Flow<Outcome<LocalMessage, GetMessageError>> {
        return messageRepository.get(id).map { outcome ->
            outcome.mapError { error ->
                when (error) {
                    GetError.NotFound -> GetMessageError.NotFound
                    GetError.ConnectionError, GetError.UnexpectedError ->
                        GetMessageError.UnexpectedError
                }
            }
        }
    }
}