package com.thebrownfoxx.neon.common.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

class Cache<in K, V>(private val externalScope: CoroutineScope) : FlowCollector<Cache.Entry<K, V>> {
    private val flows = ConcurrentHashMap<K, MutableSharedFlow<V>>()

    fun getFlow(key: K, initialize: suspend FlowCollector<V>.() -> Unit): Flow<V> {
        return flows.getOrPut(key) {
            cacheSharedFlow<V>().apply {
                externalScope.launch {
                    initialize()
                    removeWhenUnsubscribed(key)
                }
            }
        }
            .asSharedFlow()
            .distinctUntilChanged()
    }

    suspend fun emit(key: K, value: V) {
        flows[key]?.emit(value)
    }

    override suspend fun emit(value: Entry<K, V>) {
        emit(value.key, value.value)
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

    data class Entry<out K, out V>(
        val key: K,
        val value: V,
    )
}

class SingleCache<V>(private val externalScope: CoroutineScope) : FlowCollector<V> {
    private var flow: MutableSharedFlow<V>? = null

    fun getFlow(initialize: suspend FlowCollector<V>.() -> Unit): Flow<V> {
        val flow = flow ?: cacheSharedFlow<V>().apply {
            flow = this
            externalScope.launch {
                initialize()
            }
        }
        return flow
            .asSharedFlow()
            .distinctUntilChanged()
    }

    override suspend fun emit(value: V) {
        flow?.emit(value)
    }
}

private fun <V> cacheSharedFlow() = MutableSharedFlow<V>(replay = 1, extraBufferCapacity = 16)