package com.thebrownfoxx.neon.client.service.offinefirst.mesage

import com.thebrownfoxx.neon.client.model.LocalChatPreviews
import com.thebrownfoxx.neon.client.model.LocalDelivery
import com.thebrownfoxx.neon.client.model.LocalMessage
import com.thebrownfoxx.neon.client.model.LocalTimestampedMessageId
import com.thebrownfoxx.neon.client.remote.RemoteMessenger
import com.thebrownfoxx.neon.client.repository.LocalMessageRepository
import com.thebrownfoxx.neon.client.service.Authenticator
import com.thebrownfoxx.neon.client.service.Messenger
import com.thebrownfoxx.neon.client.service.Messenger.GetChatPreviewsError
import com.thebrownfoxx.neon.client.service.Messenger.GetMessageError
import com.thebrownfoxx.neon.client.service.Messenger.GetMessagesError
import com.thebrownfoxx.neon.client.service.Messenger.MarkAsReadError
import com.thebrownfoxx.neon.client.service.Messenger.SendMessageError
import com.thebrownfoxx.neon.client.service.offinefirst.offlineFirstFlow
import com.thebrownfoxx.neon.common.data.Cache
import com.thebrownfoxx.neon.common.data.DataOperationError
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.data.SingleCache
import com.thebrownfoxx.neon.common.extension.ExponentialBackoff
import com.thebrownfoxx.neon.common.extension.ExponentialBackoffValues
import com.thebrownfoxx.neon.common.extension.flow.mirrorTo
import com.thebrownfoxx.neon.common.extension.loop
import com.thebrownfoxx.neon.common.logError
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MessageId
import com.thebrownfoxx.outcome.Failure
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.UnitOutcome
import com.thebrownfoxx.outcome.map.mapError
import com.thebrownfoxx.outcome.map.onFailure
import com.thebrownfoxx.outcome.map.onSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.seconds
import com.thebrownfoxx.neon.client.remote.RemoteMessenger.SendMessageError as RemoteSendMessageError

class OfflineFirstMessenger(
    private val authenticator: Authenticator,
    private val remoteMessenger: RemoteMessenger,
    private val localMessageRepository: LocalMessageRepository,
    externalScope: CoroutineScope,
) : Messenger {
    private val sendMessageExponentialBackoffValues = ExponentialBackoffValues(
        initialDelay = 1.seconds,
        maxDelay = 32.seconds,
        factor = 2.0,
    )

    // TODO: Maybe type-alias the Outcome<x, y>s? Something like GetChatPreviewsOutcome
    private val chatPreviewsCache =
        SingleCache<Outcome<LocalChatPreviews, GetChatPreviewsError>>(externalScope)
    private val messagesCache =
        Cache<GroupId, Outcome<List<LocalTimestampedMessageId>, GetMessagesError>>(externalScope)
    private val messageCache =
        Cache<MessageId, Outcome<LocalMessage, GetMessageError>>(externalScope)

    override val chatPreviews: Flow<Outcome<LocalChatPreviews, GetChatPreviewsError>> =
        chatPreviewsCache.getOrInitialize {
            offlineFirstFlow(
                localFlow = localMessageRepository.chatPreviews,
                remoteFlow = remoteMessenger.chatPreviews,
                handler = ChatPreviewsOfflineFirstHandler(localMessageRepository),
            ).mirrorTo(this) { chatPreviewsOutcome ->
                chatPreviewsOutcome.mapError { it.toGetChatPreviewsError() }
            }
        }

    override fun getMessages(
        groupId: GroupId,
    ): Flow<Outcome<List<LocalTimestampedMessageId>, GetMessagesError>> {
        return messagesCache.getOrInitialize(groupId) {
            offlineFirstFlow(
                localFlow = localMessageRepository.getMessagesAsFlow(groupId),
                remoteFlow = remoteMessenger.getMessages(groupId),
                handler = MessagesOfflineFirstHandler(groupId, localMessageRepository),
            ).mirrorTo(this) { messagesOutcome ->
                messagesOutcome.mapError { it.toGetMessagesError() }
            }
        }
    }

    override fun getMessage(id: MessageId): Flow<Outcome<LocalMessage, GetMessageError>> {
        return messageCache.getOrInitialize(id) {
            offlineFirstFlow(
                localFlow = localMessageRepository.getMessageAsFlow(id),
                remoteFlow = remoteMessenger.getMessage(id),
                handler = MessageOfflineFirstHandler(localMessageRepository),
            ).mirrorTo(this) { messageOutcome ->
                messageOutcome.mapError { it.toGetMessageError() }
            }
        }
    }

    override suspend fun sendMessage(
        id: MessageId,
        groupId: GroupId,
        content: String,
    ): UnitOutcome<SendMessageError> {
        val loggedInMemberId = authenticator.loggedInMemberId.value
            ?: return Failure(SendMessageError.Unauthorized)
        val localMessage = LocalMessage(
            id = id,
            groupId = groupId,
            senderId = loggedInMemberId,
            content = content,
            timestamp = Clock.System.now(),
            delivery = LocalDelivery.Sending,
        )
        return localMessageRepository.upsert(localMessage)
            .mapError { SendMessageError.UnexpectedError }
    }

    override suspend fun markAsRead(groupId: GroupId): UnitOutcome<MarkAsReadError> {
        TODO("Not yet implemented")
    }

    private suspend fun sendOutgoingMessages() {
        while (true) {
            val outgoingMessage = localMessageRepository.outgoingQueue.receive()
            val exponentialBackoff = ExponentialBackoff(sendMessageExponentialBackoffValues)
            loop {
                remoteMessenger.sendMessage(
                    id = outgoingMessage.id,
                    groupId = outgoingMessage.groupId,
                    content = outgoingMessage.content,
                )
                    .onSuccess { breakLoop() }
                    .onFailure { error ->
                        onSendFailure(error, outgoingMessage, onDone = { breakLoop() })
                    }
                exponentialBackoff.delay()
            }
        }
    }

    private suspend fun onSendFailure(
        error: RemoteSendMessageError,
        outgoingMessage: LocalMessage,
        onDone: () -> Unit,
    ) {
        when (error) {
            RemoteSendMessageError.DuplicateId -> onDone()
            RemoteSendMessageError.UnexpectedError, RemoteSendMessageError.RequestTimeout -> {}
            RemoteSendMessageError.Unauthorized, RemoteSendMessageError.GroupNotFound -> {
                val failedMessage = outgoingMessage.copy(delivery = LocalDelivery.Failed)
                localMessageRepository.upsert(failedMessage).onFailure { logError() }
                onDone()
            }
        }
    }

    private fun DataOperationError.toGetChatPreviewsError() = when (this) {
        DataOperationError.ConnectionError, DataOperationError.UnexpectedError ->
            GetChatPreviewsError.UnexpectedError
    }

    private fun DataOperationError.toGetMessagesError() = when (this) {
        DataOperationError.ConnectionError, DataOperationError.UnexpectedError ->
            GetMessagesError.UnexpectedError
    }

    private fun GetError.toGetMessageError() = when (this) {
        GetError.NotFound -> GetMessageError.NotFound
        GetError.ConnectionError, GetError.UnexpectedError -> GetMessageError.UnexpectedError
    }
}