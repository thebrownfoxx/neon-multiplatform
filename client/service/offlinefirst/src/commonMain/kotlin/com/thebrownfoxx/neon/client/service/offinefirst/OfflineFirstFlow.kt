package com.thebrownfoxx.neon.client.service.offinefirst

import com.thebrownfoxx.neon.common.extension.flow.channelFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Creates a cold flow from [localFlow] that, when collected, will emit the latest successful value
 * of [localFlow].
 *
 * The caller must provide it a [handler] that implements [OfflineFirstHandler.updateLocal] to
 * handle the update of the local value (like updating the repository that provides [localFlow]).
 *
 * A failed value from [localFlow], which is identified by [OfflineFirstHandler.hasLocalFailed], is
 * only emitted when [OfflineFirstHandler.updateLocal] has already been called from an emission of
 * [remoteFlow]. This is to avoid emitting a failed value caused by the local value not being
 * up-to-date from remote (e.g., showing not found since it has never been cached locally before,
 * but exists in remote).
 *
 * [OfflineFirstHandler.updateLocal] is only called after [localFlow] has already emitted once, so
 * the caller can decide if/how to update local.
 */
fun <TL, TR> offlineFirstFlow(
    localFlow: Flow<TL>,
    remoteFlow: Flow<TR>,
    handler: OfflineFirstHandler<TL, TR>,
): Flow<TL> {
    return channelFlow {
        val local = MutableSharedFlow<TL>(replay = 1)
        val updatedFromRemote = MutableStateFlow(false)
        with(handler) {
            launch {
                localFlow.collect { newLocal ->
                    local.emit(newLocal)
                    if (!hasLocalFailed(newLocal)) emit(newLocal)
                }
            }
            launch {
                remoteFlow.collect { newRemote ->
                    updateLocal(newRemote, local.first())
                    updatedFromRemote.value = true
                }
            }
            launch {
                combine(local, updatedFromRemote) { local, updatedFromRemote ->
                    if (updatedFromRemote) emit(local)
                }.collect()
            }
        }
    }.distinctUntilChanged()
}

interface OfflineFirstHandler<TL, TR> {
    fun hasLocalFailed(local: TL): Boolean
    suspend fun updateLocal(newRemote: TR, oldLocal: TL)
}