package com.thebrownfoxx.neon.client.service.offinefirst.mesage

import com.thebrownfoxx.neon.client.model.LocalChatPreviews
import com.thebrownfoxx.neon.client.model.LocalDelivery
import com.thebrownfoxx.neon.client.model.LocalMessage
import com.thebrownfoxx.neon.client.model.LocalTimestampedMessageId
import com.thebrownfoxx.neon.client.remote.RemoteMessenger
import com.thebrownfoxx.neon.client.repository.LocalDeliveryRepository
import com.thebrownfoxx.neon.client.repository.LocalMessageRepository
import com.thebrownfoxx.neon.client.service.Authenticator
import com.thebrownfoxx.neon.client.service.Messenger
import com.thebrownfoxx.neon.client.service.Messenger.*
import com.thebrownfoxx.neon.client.service.offinefirst.OfflineFirstProvider
import com.thebrownfoxx.neon.common.data.DataOperationError
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.extension.ExponentialBackoff
import com.thebrownfoxx.neon.common.extension.ExponentialBackoffValues
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.seconds
import com.thebrownfoxx.neon.client.remote.RemoteMessenger.SendMessageError as RemoteSendMessageError

@OptIn(ExperimentalCoroutinesApi::class)
class OfflineFirstMessenger(
    private val authenticator: Authenticator,
    private val remoteMessenger: RemoteMessenger,
    private val localMessageRepository: LocalMessageRepository,
    private val localDeliveryRepository: LocalDeliveryRepository,
    private val externalScope: CoroutineScope,
) : Messenger {
    private val sendMessageExponentialBackoffValues = ExponentialBackoffValues(
        initialDelay = 1.seconds,
        maxDelay = 32.seconds,
        factor = 2.0,
    )

    private val messagesCache = createMessagesCache(externalScope)
    private val messageCache = createMessageCache(externalScope)
    private val deliveryCache = createDeliveryCache(externalScope)

    init {
        externalScope.launch { sendOutgoingMessages() }
    }

    override val chatPreviews: Flow<Outcome<LocalChatPreviews, GetChatPreviewsError>> =
        OfflineFirstProvider(
            localFlow = localMessageRepository.chatPreviews,
            remoteFlow = remoteMessenger.chatPreviews,
            handler = ChatPreviewsOfflineFirstHandler(localMessageRepository),
            externalScope = externalScope,
        ).getAsMappedFlow { chatPreviewsOutcome ->
            chatPreviewsOutcome.mapError { it.toGetChatPreviewsError() }
        }

    override fun getMessages(
        groupId: GroupId,
    ): Flow<Outcome<List<LocalTimestampedMessageId>, GetMessagesError>> {
        return messagesCache.getOrPut(groupId) {
            OfflineFirstProvider(
                localFlow = localMessageRepository.getMessagesAsFlow(groupId),
                remoteFlow = remoteMessenger.getMessages(groupId),
                handler = MessagesOfflineFirstHandler(groupId, localMessageRepository),
                externalScope = externalScope,
            )
        }.getAsMappedFlow {  messagesOutcome ->
            messagesOutcome.mapError { it.toGetMessagesError() }
        }
    }

    override fun getMessage(id: MessageId): Flow<Outcome<LocalMessage, GetMessageError>> {
        return messageCache.getOrPut(id) {
            OfflineFirstProvider(
                localFlow = localMessageRepository.getMessageAsFlow(id),
                remoteFlow = remoteMessenger.getMessage(id),
                handler = MessageOfflineFirstHandler(localMessageRepository),
                externalScope = externalScope,
            )
        }.getAsMappedFlow { messageOutcome ->
            messageOutcome.mapError { it.toGetMessageError() }
        }
    }

    override fun getDelivery(id: MessageId): Flow<Outcome<LocalDelivery, GetDeliveryError>> {
        return authenticator.loggedInMemberId.flatMapLatest { memberId ->
            if (memberId == null)
                return@flatMapLatest flowOf(Failure(GetDeliveryError.Unauthorized))

            deliveryCache.getOrPut(id) {
                OfflineFirstProvider(
                    localFlow = localDeliveryRepository.getAsFlow(id, memberId),
                    remoteFlow = remoteMessenger.getDelivery(id),
                    handler = DeliveryOfflineFirstHandler(id, memberId, localDeliveryRepository),
                    externalScope = externalScope,
                )
            }.getAsMappedFlow { deliveryOutcome ->
                deliveryOutcome.mapError { it.toGetDeliveryError() }
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

    private fun DataOperationError.toGetDeliveryError() = when (this) {
        DataOperationError.ConnectionError, DataOperationError.UnexpectedError ->
            GetDeliveryError.UnexpectedError
    }
}