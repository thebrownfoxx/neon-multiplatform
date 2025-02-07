package com.thebrownfoxx.neon.client.service.offinefirst

import com.thebrownfoxx.neon.common.data.cacheSharedFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch

class OfflineFirstProvider<TL, TR>(
    localFlow: Flow<TL>,
    private val remoteFlow: Flow<TR>,
    private val handler: OfflineFirstHandler<TL, TR>,
    externalScope: CoroutineScope,
) {
    private val output = cacheSharedFlow<TL>()
    private val local = localFlow.shareIn(externalScope, SharingStarted.Eagerly, replay = 1)
    private val updatedFromRemote = MutableStateFlow(false)

    val subscriptionCount = output.subscriptionCount

    init {
        externalScope.launch {
            local.collect { println(it) }
        }
        externalScope.launch { updateLocalFromRemote() }
        externalScope.launch { updateOutputFromLocal() }
    }

    fun getAsFlow(): Flow<TL> {
        return output.distinctUntilChanged()
    }

    private suspend fun updateLocalFromRemote() {
        remoteFlow.collect { newRemote ->
            updateLocal(newRemote, local.first())
            updatedFromRemote.value = true
        }
    }

    private suspend fun updateOutputFromLocal() {
        combine(local, updatedFromRemote) { newLocal, updatedFromRemote ->
            if (!hasLocalFailed(newLocal) || updatedFromRemote) output.emit(newLocal)
        }.collect()
    }

    private fun hasLocalFailed(local: TL): Boolean = handler.hasLocalFailed(local)
    private suspend fun updateLocal(newRemote: TR, oldLocal: TL) =
        handler.updateLocal(newRemote, oldLocal)
}