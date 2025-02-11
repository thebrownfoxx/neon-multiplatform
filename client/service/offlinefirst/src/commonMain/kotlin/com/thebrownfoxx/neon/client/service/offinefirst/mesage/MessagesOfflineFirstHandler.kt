package com.thebrownfoxx.neon.client.service.offinefirst.mesage

import com.thebrownfoxx.neon.client.converter.toLocalTimestampedMessageId
import com.thebrownfoxx.neon.client.model.LocalTimestampedMessageId
import com.thebrownfoxx.neon.client.remote.RemoteMessenger.GetMessagesError
import com.thebrownfoxx.neon.client.repository.LocalMessageRepository
import com.thebrownfoxx.neon.client.service.offinefirst.OfflineFirstHandler
import com.thebrownfoxx.neon.client.service.offinefirst.offlineFirstCacheMap
import com.thebrownfoxx.neon.common.data.DataOperationError
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.server.model.TimestampedMessageId
import com.thebrownfoxx.outcome.Failure
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.Success
import com.thebrownfoxx.outcome.map.onSuccess
import kotlinx.coroutines.CoroutineScope

internal fun createMessagesCache(externalScope: CoroutineScope) =
    offlineFirstCacheMap<GroupId, RepositoryMessages, RemoteMessages>(externalScope)

internal class MessagesOfflineFirstHandler(
    private val groupId: GroupId,
    private val localMessageRepository: LocalMessageRepository,
) : OfflineFirstHandler<RepositoryMessages, RemoteMessages> {
    override fun hasLocalFailed(local: RepositoryMessages): Boolean {
        return local !is Success || local.value.isEmpty()
    }

    override suspend fun updateLocal(newRemote: RemoteMessages, oldLocal: RepositoryMessages) {
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
        remoteMessages: List<TimestampedMessageId>,
        oldLocal: RepositoryMessages,
    ) {
        val localTimestampedIds = remoteMessages.map { it.toLocalTimestampedMessageId(groupId) }
        localMessageRepository.batchUpsertTimestampedIds(localTimestampedIds)
        oldLocal.onSuccess { localMessageIds ->
            // TODO: Remove deleted messages. However, we have to save local messages that have
            //  not been sent yet
//            val removedMessages = localMessageIds.filter { localMessage ->
//                remoteMessages.none { it.id == localMessage.id }
//            }
//            if (removedMessages.isNotEmpty()) TODO("Remove $removedMessages")
        }
    }
}

private typealias RepositoryMessages = Outcome<List<LocalTimestampedMessageId>, DataOperationError>
private typealias RemoteMessages = Outcome<List<TimestampedMessageId>, GetMessagesError>