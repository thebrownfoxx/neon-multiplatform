package com.thebrownfoxx.neon.common.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

class ReactiveCache<in K, out V>(
    private val scope: CoroutineScope,
    private val gettable: Gettable<K, V>,
) {
    private val flows = ConcurrentHashMap<K, MutableSharedFlow<V>>()

    fun getAsFlow(key: K): Flow<V> {
        return flows.getOrPut(key) {
            MutableSharedFlow<V>(replay = 1).also {
                scope.launch { 
                    updateCache(key)
                    it.removeWhenNoSubscribers(key)
                }
            }
        }
    }

    suspend fun updateCache(key: K) {
        flows[key]?.emit(gettable.get(key))
    }

    private suspend fun MutableSharedFlow<V>.removeWhenNoSubscribers(key: K) {
        subscriptionCount.drop(1).collect { if (it == 0) flows.remove(key) }
    }
}