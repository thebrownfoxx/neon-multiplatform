package com.thebrownfoxx.neon.client.service.offinefirst.mesage

import com.thebrownfoxx.neon.client.model.LocalTimestampedMessageId
import com.thebrownfoxx.neon.client.repository.MessageRepository
import com.thebrownfoxx.neon.client.service.Messenger.GetMessagesError
import com.thebrownfoxx.neon.client.service.offinefirst.OfflineFirstHandler
import com.thebrownfoxx.neon.common.data.DataOperationError
import com.thebrownfoxx.outcome.Failure
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.Success
import com.thebrownfoxx.outcome.map.onSuccess

class MessagesOfflineFirstHandler(
    private val localMessageRepository: MessageRepository,
) : OfflineFirstHandler<RepositoryMessages, ServiceMessages> {
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
        if (remoteError == GetMessagesError.GroupNotFound && oldLocal is Success) {
            TODO("Delete ${oldLocal.value}")
        }
    }

    private suspend fun onRemoteSuccess(
        remoteMessages: List<LocalTimestampedMessageId>,
        oldLocal: RepositoryMessages,
    ) {
        localMessageRepository.batchUpsertTimestampedIds(remoteMessages)
        oldLocal.onSuccess { localMessageIds ->
            val removedMessages = localMessageIds.filter { it !in remoteMessages }
            if (removedMessages.isNotEmpty()) TODO("Removed $removedMessages")
        }
    }
}

private typealias RepositoryMessages = Outcome<List<LocalTimestampedMessageId>, DataOperationError>
private typealias ServiceMessages = Outcome<List<LocalTimestampedMessageId>, GetMessagesError>