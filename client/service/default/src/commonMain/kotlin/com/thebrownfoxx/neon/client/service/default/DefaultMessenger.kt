package com.thebrownfoxx.neon.client.service.default

import com.thebrownfoxx.neon.client.model.LocalConversationPreviews
import com.thebrownfoxx.neon.client.model.LocalDelivery
import com.thebrownfoxx.neon.client.model.LocalMessage
import com.thebrownfoxx.neon.client.repository.MessageRepository
import com.thebrownfoxx.neon.client.service.Authenticator
import com.thebrownfoxx.neon.client.service.Messenger
import com.thebrownfoxx.neon.client.service.Messenger.GetConversationPreviewsError
import com.thebrownfoxx.neon.client.service.Messenger.GetMessageError
import com.thebrownfoxx.neon.client.service.Messenger.GetMessagesError
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MessageId
import com.thebrownfoxx.neon.common.websocket.OldWebSocketSession
import com.thebrownfoxx.neon.server.route.websocket.message.SendMessageRequest
import com.thebrownfoxx.outcome.Failure
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.Success
import com.thebrownfoxx.outcome.UnitOutcome
import com.thebrownfoxx.outcome.map.mapError
import com.thebrownfoxx.outcome.map.onSuccess
import com.thebrownfoxx.outcome.map.transform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.datetime.Clock

class DefaultMessenger(
    private val authenticator: Authenticator,
    private val messageRepository: MessageRepository,
    private val webSocketSession: OldWebSocketSession,
) : Messenger {
    private val coroutineScope = CoroutineScope(Dispatchers.IO) + SupervisorJob()
    private val outgoingMessages = mutableListOf<MessageId>()

    init {
        coroutineScope.launch {
            messageRepository.getOutgoingMessagesAsFlow().collect { messagesOutcome ->
                messagesOutcome.onSuccess { messages ->
                    messages.forEach { message ->
                        if (message.id in outgoingMessages) return@forEach
                        uploadMessage(
                            id = message.id,
                            groupId = message.groupId,
                            content = message.content,
                        )
                        outgoingMessages.add(message.id)
                    }
                }
            }
        }
    }

    override val conversationPreviews:
            Flow<Outcome<LocalConversationPreviews, GetConversationPreviewsError>> =
        messageRepository.conversationPreviewsFlow.map { outcome ->
        outcome.mapError { GetConversationPreviewsError.UnexpectedError }
    }

    override fun getMessages(
        groupId: GroupId,
    ): Flow<Outcome<Set<MessageId>, GetMessagesError>> {
        return messageRepository.getMessagesAsFlow(groupId).map { outcome ->
            outcome.mapError { GetMessagesError.UnexpectedError }
        }
    }

    override fun getMessage(id: MessageId): Flow<Outcome<LocalMessage, GetMessageError>> {
        return messageRepository.getAsFlow(id).map { outcome ->
            outcome.mapError { error ->
                when (error) {
                    GetError.NotFound -> GetMessageError.NotFound
                    GetError.ConnectionError, GetError.UnexpectedError ->
                        GetMessageError.UnexpectedError
                }
            }
        }
    }

    override suspend fun sendMessage(groupId: GroupId, content: String): UnitOutcome<Unit> {
        val sender = authenticator.loggedInMember.value ?: return Failure(Unit)

        val message = LocalMessage(
            groupId = groupId,
            content = content,
            delivery = LocalDelivery.Sending,
            senderId = sender,
            timestamp = Clock.System.now()
        )
        return messageRepository.upsert(message).mapError {}
    }

    private suspend fun uploadMessage(
        id: MessageId,
        groupId: GroupId,
        content: String,
    ): UnitOutcome<Unit> {
        return webSocketSession.send(
            SendMessageRequest(
                id = id,
                groupId = groupId,
                content = content,
            )
        ).transform(
            onSuccess = { response ->
                // TODO: Handle these
                Success(Unit)
            },
            onFailure = { error ->
                // TODO: Handle these
                Failure(Unit)
            },
        )
    }
}