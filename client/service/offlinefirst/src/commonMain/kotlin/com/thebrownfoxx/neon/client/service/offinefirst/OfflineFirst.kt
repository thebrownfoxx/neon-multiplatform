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

fun <TL, TR, R> offlineFirstFlow(
    localFlow: Flow<TL>,
    remoteFlow: Flow<TR>,
    handler: OfflineFirstHandler<TL, TR, R>,
): Flow<R> {
    return channelFlow {
        val local = MutableSharedFlow<TL>(replay = 1)
        val updatedFromRemote = MutableStateFlow(false)
        with(handler) {
            launch {
                localFlow.collect { newLocal ->
                    local.emit(newLocal)
                    if (!hasLocalFailed(newLocal)) emit(mapLocal(newLocal))
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
                    if (updatedFromRemote) emit(mapLocal(local))
                }.collect()
            }
        }
    }.distinctUntilChanged()
}

interface OfflineFirstHandler<TL, TR, R> {
    fun mapLocal(local: TL): R
    fun hasLocalFailed(local: TL): Boolean
    suspend fun updateLocal(newRemote: TR, oldLocal: TL)
}