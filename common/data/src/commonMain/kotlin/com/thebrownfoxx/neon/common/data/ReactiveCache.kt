package com.thebrownfoxx.neon.common.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

class ReactiveCache<in K, out V>(
    private val scope: CoroutineScope,
    private val gettable: Gettable<K, V>,
) {
    private val flows = ConcurrentHashMap<K, MutableSharedFlow<V>>()

    fun getAsFlow(key: K): Flow<V> {
        return flows.getOrPut(key) {
            MutableSharedFlow<V>(replay = 1).apply {
                scope.launch {
                    updateCache(key)
                    // Implement flow removal :D
                }
            }
        }
    }

    suspend fun updateCache(key: K) {
        flows[key]?.updateCache(key)
    }

    private suspend fun MutableSharedFlow<V>.updateCache(key: K) {
        emit(gettable.get(key))
    }
}