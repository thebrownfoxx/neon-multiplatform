package com.thebrownfoxx.neon.client.service.offinefirst.mesage

import com.thebrownfoxx.neon.client.model.LocalChatPreviews
import com.thebrownfoxx.neon.client.model.LocalDelivery
import com.thebrownfoxx.neon.client.model.LocalMessage
import com.thebrownfoxx.neon.client.repository.MessageRepository
import com.thebrownfoxx.neon.client.service.Messenger.GetChatPreviewsError
import com.thebrownfoxx.neon.client.service.offinefirst.OfflineFirstHandler
import com.thebrownfoxx.neon.common.data.DataOperationError
import com.thebrownfoxx.outcome.Failure
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.Success

class ChatPreviewsOfflineFirstHandler(
    private val localMessageRepository: MessageRepository,
) : OfflineFirstHandler<RepositoryChatPreviews, ServiceChatPreviews> {
    override fun hasLocalFailed(local: RepositoryChatPreviews): Boolean {
        return local !is Success || local.value.toFlatList().isEmpty()
    }

    override suspend fun updateLocal(
        newRemote: ServiceChatPreviews,
        oldLocal: RepositoryChatPreviews,
    ) {
        when (newRemote) {
            is Failure -> onRemoteFailure(newRemote.error, oldLocal)
            is Success -> onRemoteSuccess(newRemote.value.toFlatList(), oldLocal)
        }
    }

    private fun onRemoteFailure(
        remoteError: GetChatPreviewsError,
        oldLocal: RepositoryChatPreviews,
    ) {
        if (remoteError != GetChatPreviewsError.MemberNotFound || oldLocal !is Success) return
        TODO("Delete ${oldLocal.value.toFlatList()}")
    }

    private suspend fun onRemoteSuccess(
        remoteChatPreviews: List<LocalMessage>,
        oldLocal: RepositoryChatPreviews,
    ) {
        localMessageRepository.batchUpsert(remoteChatPreviews)
        if (oldLocal !is Success) return
        val removedChatPreviews = oldLocal.value.toFlatList()
            .filter { it.delivery != LocalDelivery.Sending && it !in remoteChatPreviews }
        if (removedChatPreviews.isNotEmpty()) TODO("Removed $removedChatPreviews")
    }
}

private typealias RepositoryChatPreviews = Outcome<LocalChatPreviews, DataOperationError>
private typealias ServiceChatPreviews = Outcome<LocalChatPreviews, GetChatPreviewsError>