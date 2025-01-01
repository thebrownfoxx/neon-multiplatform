package com.thebrownfoxx.neon.common.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

class Cache<in K, V>(private val externalScope: CoroutineScope) {
    private val flows = ConcurrentHashMap<K, MutableSharedFlow<V>>()

    fun getAsFlow(key: K, initialize: suspend FlowCollector<V>.() -> Unit): Flow<V> {
        return flows.getOrPut(key) {
            MutableSharedFlow<V>(replay = 1).apply {
                externalScope.launch {
                    initialize()
                    removeWhenUnsubscribed(key)
                }
            }
        }.asSharedFlow()
    }

    fun emit(key: K, value: V) {
        flows[key]?.let { externalScope.launch { it.emit(value) } }
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

class SingleCache<V>(private val externalScope: CoroutineScope) {
    private var flow: MutableSharedFlow<V>? = null

    fun getAsFlow(initialize: suspend FlowCollector<V>.() -> Unit): Flow<V> {
        val flow = flow ?: MutableSharedFlow<V>(replay = 1).apply {
            flow = this
            externalScope.launch {
                initialize()
            }
        }
        return flow.asSharedFlow()
    }

    fun emit(value: V) {
        flow?.let { externalScope.launch { it.emit(value) } }
    }
}