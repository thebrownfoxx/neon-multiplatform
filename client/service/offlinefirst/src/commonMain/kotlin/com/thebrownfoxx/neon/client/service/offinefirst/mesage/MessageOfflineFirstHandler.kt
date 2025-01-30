package com.thebrownfoxx.neon.client.service.offinefirst.mesage

import com.thebrownfoxx.neon.client.model.LocalMessage
import com.thebrownfoxx.neon.client.repository.MessageRepository
import com.thebrownfoxx.neon.client.service.Messenger.GetMessageError
import com.thebrownfoxx.neon.client.service.offinefirst.OfflineFirstHandler
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.outcome.Failure
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.Success

class MessageOfflineFirstHandler(
    private val localMessageRepository: MessageRepository,
) : OfflineFirstHandler<RepositoryMessage, ServiceMessage> {
    override fun hasLocalFailed(local: RepositoryMessage): Boolean {
        return local is Failure
    }

    override suspend fun updateLocal(newRemote: ServiceMessage, oldLocal: RepositoryMessage) {
        when (newRemote) {
            is Failure -> onRemoteFailure(newRemote.error, oldLocal)
            is Success -> localMessageRepository.upsert(newRemote.value)
        }
    }

    private fun onRemoteFailure(
        remoteError: GetMessageError,
        oldLocal: RepositoryMessage,
    ) {
        val deletableErrors = setOf(GetMessageError.Unauthorized, GetMessageError.NotFound)
        if (remoteError in deletableErrors && oldLocal is Success) {
            TODO("Delete ${oldLocal.value}")
        }
    }
}

private typealias RepositoryMessage = Outcome<LocalMessage, GetError>
private typealias ServiceMessage = Outcome<LocalMessage, GetMessageError>