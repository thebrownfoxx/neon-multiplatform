package com.thebrownfoxx.neon.client.service.offinefirst

import com.thebrownfoxx.neon.client.model.LocalConversationPreviews
import com.thebrownfoxx.neon.client.model.LocalDelivery
import com.thebrownfoxx.neon.client.model.LocalMessage
import com.thebrownfoxx.neon.client.model.LocalTimestampedMessageId
import com.thebrownfoxx.neon.client.repository.MessageRepository
import com.thebrownfoxx.neon.client.service.Authenticator
import com.thebrownfoxx.neon.client.service.Messenger
import com.thebrownfoxx.neon.client.service.Messenger.GetConversationPreviewsError
import com.thebrownfoxx.neon.client.service.Messenger.GetMessageError
import com.thebrownfoxx.neon.client.service.Messenger.GetMessagesError
import com.thebrownfoxx.neon.client.service.Messenger.SendMessageError
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.extension.ExponentialBackoff
import com.thebrownfoxx.neon.common.extension.ExponentialBackoffValues
import com.thebrownfoxx.neon.common.extension.failedWith
import com.thebrownfoxx.neon.common.extension.loop
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MessageId
import com.thebrownfoxx.outcome.Failure
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.Success
import com.thebrownfoxx.outcome.UnitOutcome
import com.thebrownfoxx.outcome.UnitSuccess
import com.thebrownfoxx.outcome.map.getOrElse
import com.thebrownfoxx.outcome.map.mapError
import com.thebrownfoxx.outcome.map.onFailure
import com.thebrownfoxx.outcome.map.onSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.seconds

class OfflineFirstMessenger(
    private val authenticator: Authenticator,
    private val remoteMessenger: Messenger,
    private val localMessageRepository: MessageRepository,
    externalScope: CoroutineScope,
) : Messenger {
    private val sendMessageExponentialBackoffValues = ExponentialBackoffValues(
        initialDelay = 1.seconds,
        maxDelay = 32.seconds,
        factor = 2.0,
    )

    init {
        externalScope.launch { sendOutgoingMessages() }
    }

    override val conversationPreviews:
            Flow<Outcome<LocalConversationPreviews, GetConversationPreviewsError>> = run {
        val mappedLocalFlow = localMessageRepository.conversationPreviews.map { local ->
            local.mapError { GetConversationPreviewsError.UnexpectedError }
        }
        offlineFirst(
            localFlow = mappedLocalFlow,
            remoteFlow = remoteMessenger.conversationPreviews,
        ) {
            transformLazy(
                localSucceeded = { it is Success && it.value.isNotEmpty() },
                localNotFound = { it is Success && it.value.isEmpty() },
                localFailedUnexpectedly = { it.failedWith(GetMessagesError.UnexpectedError) },
                remoteSucceeded = { it is Success },
                remoteNotFound = { it.failedWith(GetMessagesError.GroupNotFound) },
                remoteFailedUnexpectedly = { it.failedWith(GetMessagesError.UnexpectedError) },
                updateLocal = ::updateConversationPreviews,
            )
        }
    }

    override fun getMessages(
        groupId: GroupId,
    ): Flow<Outcome<List<LocalTimestampedMessageId>, GetMessagesError>> {
        val mappedLocalFlow = localMessageRepository.getMessagesAsFlow(groupId).map { local ->
            local.mapError { GetMessagesError.UnexpectedError }
        }
        return offlineFirst(
            localFlow = mappedLocalFlow,
            remoteFlow = remoteMessenger.getMessages(groupId),
        ) {
            transformLazy(
                localSucceeded = { it is Success && it.value.isNotEmpty() },
                localNotFound = { it is Success && it.value.isEmpty() },
                localFailedUnexpectedly = { it.failedWith(GetMessagesError.UnexpectedError) },
                remoteSucceeded = { it is Success },
                remoteNotFound = { it.failedWith(GetMessagesError.GroupNotFound) },
                remoteFailedUnexpectedly = { it.failedWith(GetMessagesError.UnexpectedError) },
                updateLocal = ::updateGroupMessages,
            )
        }
    }

    override fun getMessage(id: MessageId): Flow<Outcome<LocalMessage, GetMessageError>> {
        val mappedLocalFlow = localMessageRepository.getMessageAsFlow(id).map { local ->
            local.mapError { it.toGetMessageError() }
        }
        return offlineFirst(
            localFlow = mappedLocalFlow,
            remoteFlow = remoteMessenger.getMessage(id),
        ) {
            transformLazy(
                succeeded = { it is Success },
                notFound = { it.failedWith(GetMessageError.NotFound) },
                failedUnexpectedly = { it.failedWith(GetMessageError.UnexpectedError) },
                updateLocal = ::updateMessage,
            )
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
        localMessageRepository.upsert(localMessage)
        return UnitSuccess
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
        error: SendMessageError,
        outgoingMessage: LocalMessage,
        onDone: () -> Unit,
    ) {
        when (error) {
            SendMessageError.Unauthorized, SendMessageError.GroupNotFound -> {
                val failedMessage = outgoingMessage.copy(delivery = LocalDelivery.Failed)
                localMessageRepository.upsert(failedMessage)
                onDone()
            }

            SendMessageError.DuplicateId -> onDone()
            SendMessageError.UnexpectedError, SendMessageError.RequestTimeout -> {}
        }
    }

    private fun LocalConversationPreviews.isEmpty(): Boolean {
        return nudgedPreviews.isEmpty() && unreadPreviews.isEmpty() && readPreviews.isEmpty()
    }

    private fun LocalConversationPreviews.isNotEmpty(): Boolean {
        return !isEmpty()
    }

    private suspend fun updateConversationPreviews(
        previewsOutcome: Outcome<LocalConversationPreviews, *>,
    ) {
        val previews = previewsOutcome.getOrElse { return }
        listOf(
            previews.nudgedPreviews,
            previews.unreadPreviews,
            previews.readPreviews,
        ).forEach { localMessageRepository.batchUpsert(it) }
    }

    private suspend fun updateGroupMessages(
        timestampedMessageIdsOutcome: Outcome<List<LocalTimestampedMessageId>, *>,
    ) {
        val timestampedMessageIds = timestampedMessageIdsOutcome.getOrElse { return }
        localMessageRepository.batchUpsertTimestampedIds(timestampedMessageIds)
    }

    private fun GetError.toGetMessageError() = when (this) {
        GetError.NotFound -> GetMessageError.NotFound
        GetError.ConnectionError, GetError.UnexpectedError -> GetMessageError.UnexpectedError
    }

    private suspend fun updateMessage(
        messageOutcome: Outcome<LocalMessage, *>,
    ) {
        val message = messageOutcome.getOrElse { return }
        localMessageRepository.upsert(message)
    }
}