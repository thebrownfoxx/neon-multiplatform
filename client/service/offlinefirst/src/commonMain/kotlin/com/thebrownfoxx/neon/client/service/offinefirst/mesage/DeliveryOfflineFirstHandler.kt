package com.thebrownfoxx.neon.client.service.offinefirst.mesage

import com.thebrownfoxx.neon.client.converter.toLocalDelivery
import com.thebrownfoxx.neon.client.model.LocalDelivery
import com.thebrownfoxx.neon.client.remote.RemoteMessenger.GetDeliveryError
import com.thebrownfoxx.neon.client.repository.LocalDeliveryRepository
import com.thebrownfoxx.neon.client.service.offinefirst.OfflineFirstHandler
import com.thebrownfoxx.neon.client.service.offinefirst.offlineFirstCacheMap
import com.thebrownfoxx.neon.common.data.DataOperationError
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.common.type.id.MessageId
import com.thebrownfoxx.neon.server.model.Delivery
import com.thebrownfoxx.outcome.Failure
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.Success
import kotlinx.coroutines.CoroutineScope

internal fun createDeliveryCache(externalScope: CoroutineScope) =
    offlineFirstCacheMap<MessageId, RepositoryDelivery, RemoteDelivery>(externalScope)

internal class DeliveryOfflineFirstHandler(
    private val messageId: MessageId,
    private val memberId: MemberId,
    private val localDeliveryRepository: LocalDeliveryRepository,
) : OfflineFirstHandler<RepositoryDelivery, RemoteDelivery> {
    override fun hasLocalFailed(local: RepositoryDelivery): Boolean {
        return local is Failure
    }

    override suspend fun updateLocal(newRemote: RemoteDelivery, oldLocal: RepositoryDelivery) {
        when (newRemote) {
            is Failure -> onRemoteFailure(newRemote.error, oldLocal)
            is Success -> onRemoteSuccess(newRemote.value, oldLocal)
        }
    }

    private fun onRemoteFailure(
        remoteError: GetDeliveryError,
        oldLocal: RepositoryDelivery,
    ) {
        val deletableErrors =
            listOf(GetDeliveryError.MessageNotFound, GetDeliveryError.Unauthorized)
        val notSending = oldLocal is Success && oldLocal.value != LocalDelivery.Sending
        if (remoteError in deletableErrors && notSending) {
            TODO("Delete $oldLocal")
        }
    }


    private suspend fun onRemoteSuccess(
        remoteDelivery: Delivery,
        oldLocal: RepositoryDelivery,
    ) {
        if (oldLocal !is Success || oldLocal.value.ordinal < remoteDelivery.ordinal) {
            localDeliveryRepository.set(messageId, memberId, remoteDelivery.toLocalDelivery())
        }
    }
}

private typealias RepositoryDelivery = Outcome<LocalDelivery, DataOperationError>
private typealias RemoteDelivery = Outcome<Delivery, GetDeliveryError>