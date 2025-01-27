package com.thebrownfoxx.neon.client.service.offinefirst

import com.thebrownfoxx.neon.common.extension.flow.channelFlow
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

fun <TL, TR, R> offlineFirstFlow(
    localFlow: Flow<TL>,
    remoteFlow: Flow<TR>,
    handler: OfflineFirstHandler<TL, TR, R>,
): Flow<R> {
    return channelFlow {
        var oldLocal: Deferred<TL>
        var updatedFromRemote = false
        with(handler) {
            launch {
                localFlow.collect { newLocal ->
                    oldLocal = async { newLocal }
                    if (!hasLocalFailed(newLocal) || updatedFromRemote) emit(mapLocal(newLocal))
                }
            }
            oldLocal = async { localFlow.first() }
            launch {
                remoteFlow.collect { newRemote ->
                    updateLocal(newRemote, oldLocal.await())
                    updatedFromRemote = true
                }
            }
        }
    }
}

interface OfflineFirstHandler<TL, TR, R> {
    fun mapLocal(local: TL): R
    fun hasLocalFailed(local: TL): Boolean
    suspend fun updateLocal(newRemote: TR, oldLocal: TL)
}