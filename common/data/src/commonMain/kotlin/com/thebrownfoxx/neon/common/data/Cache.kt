package com.thebrownfoxx.neon.common.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

class Cache<in K, V>(private val coroutineScope: CoroutineScope) {
    private val flows = ConcurrentHashMap<K, MutableSharedFlow<V>>()

    fun getAsFlow(key: K, initialization: suspend MutableSharedFlow<V>.() -> Unit): Flow<V> {
        return flows.getOrPut(key) {
            MutableSharedFlow<V>(replay = 1).apply {
                coroutineScope.launch {
                    initialization()
                    removeWhenUnsubscribed(key)
                }
            }
        }
    }

    fun emit(key: K, value: V) {
        flows[key]?.let { coroutineScope.launch { it.emit(value) } }
    }

    private suspend fun MutableSharedFlow<V>.removeWhenUnsubscribed(key: K) {
        var subscribedOn = false
        subscriptionCount.collect {
            when {
                subscribedOn && it == 0 -> flows.remove(key)
                it > 0 -> subscribedOn = true
            }
        }
    }
}