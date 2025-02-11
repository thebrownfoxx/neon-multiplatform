package com.thebrownfoxx.neon.client.service.offinefirst.mesage

import com.thebrownfoxx.neon.client.converter.toLocalMessage
import com.thebrownfoxx.neon.client.model.LocalChatPreviews
import com.thebrownfoxx.neon.client.model.LocalDelivery
import com.thebrownfoxx.neon.client.remote.RemoteMessenger.GetChatPreviewsError
import com.thebrownfoxx.neon.client.repository.LocalMessageRepository
import com.thebrownfoxx.neon.client.service.offinefirst.OfflineFirstHandler
import com.thebrownfoxx.neon.common.data.DataOperationError
import com.thebrownfoxx.neon.server.model.Message
import com.thebrownfoxx.outcome.Failure
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.Success
import com.thebrownfoxx.outcome.map.onSuccess

internal class ChatPreviewsOfflineFirstHandler(
    private val localMessageRepository: LocalMessageRepository,
) : OfflineFirstHandler<RepositoryChatPreviews, RemoteChatPreviews> {
    override fun hasLocalFailed(local: RepositoryChatPreviews): Boolean {
        return local !is Success || local.value.toFlatList().isEmpty()
    }

    override suspend fun updateLocal(
        newRemote: RemoteChatPreviews,
        oldLocal: RepositoryChatPreviews,
    ) {
        when (newRemote) {
            is Failure -> onRemoteFailure(newRemote.error, oldLocal)
            is Success -> onRemoteSuccess(newRemote.value, oldLocal)
        }
    }

    private fun onRemoteFailure(
        remoteError: GetChatPreviewsError,
        oldLocal: RepositoryChatPreviews,
    ) {
        if (remoteError == GetChatPreviewsError.MemberNotFound && oldLocal is Success) {
            TODO("Delete ${oldLocal.value.toFlatList()}")
        }
    }

    private suspend fun onRemoteSuccess(
        remoteChatPreviews: List<Message>,
        oldLocal: RepositoryChatPreviews,
    ) {
        localMessageRepository.batchUpsert(remoteChatPreviews.map { it.toLocalMessage() })
        oldLocal.onSuccess { localChatPreviews ->
            val removedChatPreviews = localChatPreviews.toFlatList().filter { localMessage ->
                val notInRemote = remoteChatPreviews.none { it.id == localMessage.id }
                localMessage.delivery != LocalDelivery.Sending && notInRemote
            }
            if (removedChatPreviews.isNotEmpty()) TODO("Removed $removedChatPreviews")
        }
    }
}

private typealias RepositoryChatPreviews = Outcome<LocalChatPreviews, DataOperationError>
private typealias RemoteChatPreviews = Outcome<List<Message>, GetChatPreviewsError>