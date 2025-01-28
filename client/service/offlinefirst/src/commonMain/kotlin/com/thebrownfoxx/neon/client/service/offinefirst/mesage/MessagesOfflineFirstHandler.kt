package com.thebrownfoxx.neon.client.service.offinefirst.mesage

import com.thebrownfoxx.neon.client.model.LocalTimestampedMessageId
import com.thebrownfoxx.neon.client.repository.MessageRepository
import com.thebrownfoxx.neon.client.service.Messenger.GetMessagesError
import com.thebrownfoxx.neon.client.service.offinefirst.OfflineFirstHandler
import com.thebrownfoxx.neon.common.data.DataOperationError
import com.thebrownfoxx.outcome.Failure
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.Success
import com.thebrownfoxx.outcome.map.mapError

class MessagesOfflineFirstHandler(
    private val localMessageRepository: MessageRepository,
) : OfflineFirstHandler<RepositoryMessages, ServiceMessages, ServiceMessages> {
    override fun mapLocal(local: RepositoryMessages): ServiceMessages {
        return local.mapError { it.toGetMessagesError() }
    }

    override fun hasLocalFailed(local: RepositoryMessages): Boolean {
        return local !is Success || local.value.isEmpty()
    }

    override suspend fun updateLocal(newRemote: ServiceMessages, oldLocal: RepositoryMessages) {
        when (newRemote) {
            is Failure -> onRemoteFailure(newRemote.error, oldLocal)
            is Success -> onRemoteSuccess(newRemote.value, oldLocal)
        }
    }

    private fun onRemoteFailure(
        remoteError: GetMessagesError,
        oldLocal: RepositoryMessages,
    ) {
        if (remoteError != GetMessagesError.GroupNotFound || oldLocal !is Success) return
        TODO("Delete ${oldLocal.value}")
    }

    private suspend fun onRemoteSuccess(
        remoteMessages: List<LocalTimestampedMessageId>,
        oldLocal: RepositoryMessages,
    ) {
        localMessageRepository.batchUpsertTimestampedIds(remoteMessages)
        if (oldLocal !is Success) return
        val removedMessages = oldLocal.value.filter { it !in remoteMessages }
        if (removedMessages.isNotEmpty()) TODO("Removed $removedMessages")
    }

    private fun DataOperationError.toGetMessagesError() = when (this) {
        DataOperationError.ConnectionError, DataOperationError.UnexpectedError ->
            GetMessagesError.UnexpectedError
    }
}

private typealias RepositoryMessages = Outcome<List<LocalTimestampedMessageId>, DataOperationError>
private typealias ServiceMessages = Outcome<List<LocalTimestampedMessageId>, GetMessagesError>