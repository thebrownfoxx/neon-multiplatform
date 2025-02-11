package com.thebrownfoxx.neon.client.service.offinefirst

import com.thebrownfoxx.neon.common.data.CacheMap
import com.thebrownfoxx.neon.common.data.EvictionStrategy
import com.thebrownfoxx.neon.common.data.Evictor
import com.thebrownfoxx.neon.common.data.cacheFlow
import com.thebrownfoxx.neon.common.data.onUnsubscribe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Creates a hot flow from [localFlow] that emits the latest successful value of [localFlow],
 * accessible through [getAsFlow] and [getAsMappedFlow].
 *
 * This class expects [localFlow] to have a replay of 1, since it uses [Flow.first] to check the
 * latest local value.
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
class OfflineFirstProvider<TL, TR>(
    private val localFlow: Flow<TL>,
    private val remoteFlow: Flow<TR>,
    private val handler: OfflineFirstHandler<TL, TR>,
    externalScope: CoroutineScope,
) {
    // TODO: Find a way to synchronously copy the latest value of localFlow so we don't have to
    //  require localFlow to have a replay. Or maybe not. Maybe it's fine since we only have
    //  flows with replays anyway.

    private val output = cacheFlow<TL>()
    private val updatedFromRemote = MutableStateFlow(false)

    val subscriptionCount = output.subscriptionCount

    init {
        externalScope.launch { updateFromLocal() }
        externalScope.launch { updateLocalFromRemote() }
        externalScope.launch { updateAfterUpdatedFromRemote() }
    }

    private suspend fun updateFromLocal() {
        localFlow.collect { newLocal ->
            if (!hasLocalFailed(newLocal)) output.emit(newLocal)
        }
    }

    private suspend fun updateLocalFromRemote() {
        remoteFlow.collect { newRemote ->
            updateLocal(newRemote, localFlow.first())
            updatedFromRemote.value = true
        }
    }

    private suspend fun updateAfterUpdatedFromRemote() {
        updatedFromRemote.collect { updatedFromRemote ->
            if (updatedFromRemote) output.emit(localFlow.first())
        }
    }

    fun getAsFlow(): Flow<TL> {
        return output.distinctUntilChanged()
    }

    fun <T> getAsMappedFlow(function: (TL) -> T): Flow<T> {
        return getAsFlow().map { function(it) }
    }

    private fun hasLocalFailed(local: TL): Boolean = handler.hasLocalFailed(local)
    private suspend fun updateLocal(newRemote: TR, oldLocal: TL) =
        handler.updateLocal(newRemote, oldLocal)

    class EvictOnUnsubscribeStrategy<TL> : EvictionStrategy<OfflineFirstProvider<TL, *>> {
        override suspend fun Evictor.initializeEvictor(value: OfflineFirstProvider<TL, *>) {
            onUnsubscribe(value.subscriptionCount) { evict() }
        }
    }
}

interface OfflineFirstHandler<TL, TR> {
    fun hasLocalFailed(local: TL): Boolean
    suspend fun updateLocal(newRemote: TR, oldLocal: TL)
}

fun <K, TL, TR> offlineFirstCacheMap(
    externalScope: CoroutineScope,
) = CacheMap<K, OfflineFirstProvider<TL, TR>>(
    evictionStrategy = OfflineFirstProvider.EvictOnUnsubscribeStrategy(),
    externalScope = externalScope,
)