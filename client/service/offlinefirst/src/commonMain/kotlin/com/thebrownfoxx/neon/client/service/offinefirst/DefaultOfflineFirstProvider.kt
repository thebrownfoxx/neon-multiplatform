package com.thebrownfoxx.neon.client.service.offinefirst

import com.thebrownfoxx.neon.common.data.Cache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Class-based alternative to [offlineFirstFlow]
 * I'm leaving this in just in case you change your mind, but [offlineFirstFlow] is much simpler,
 * especially since it's a cold flow, so you don't need to pass a CoroutineScope in.
 * Also, [offlineFirstFlow] works better with your other architecture, like [Cache].
 */
@Deprecated("Use offlineFirstFlow instead")
class DefaultOfflineFirstProvider<TL, TR>(
    private val localFlow: Flow<TL>,
    private val remoteFlow: Flow<TR>,
    private val handler: OfflineFirstHandler<TL, TR>,
    externalScope: CoroutineScope,
) {
    private val output = MutableSharedFlow<TL>(replay = 1, extraBufferCapacity = 16)
    private val local = MutableSharedFlow<TL>(replay = 1)
    private val updatedFromRemote = MutableStateFlow(false)

    init {
        externalScope.launch { collectLocal() }
        externalScope.launch { collectRemote() }
        externalScope.launch { collectLocalOnRemoteUpdate() }
    }

    fun getAsFlow(): Flow<TL> {
        return output.distinctUntilChanged()
    }

    private suspend fun collectLocal() {
        localFlow.collect { newLocal ->
            local.emit(newLocal)
            if (!hasLocalFailed(newLocal)) output.emit(newLocal)
        }
    }

    private suspend fun collectRemote() {
        remoteFlow.collect { newRemote ->
            updateLocal(newRemote, local.first())
            updatedFromRemote.value = true
        }
    }

    private suspend fun collectLocalOnRemoteUpdate() {
        combine(local, updatedFromRemote) { local, updatedFromRemote ->
            if (updatedFromRemote) output.emit(local)
        }.collect()
    }

    private fun hasLocalFailed(local: TL): Boolean = handler.hasLocalFailed(local)
    private suspend fun updateLocal(newRemote: TR, oldLocal: TL) =
        handler.updateLocal(newRemote, oldLocal)
}