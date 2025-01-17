package com.thebrownfoxx.neon.common.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class Cache<in K, V>(
    private val externalScope: CoroutineScope,
    private val removalDelay: Duration = 1.minutes,
) : FlowCollector<Cache.Entry<K, V>> {
    private val flows = ConcurrentHashMap<K, MutableSharedFlow<V>>()

    fun get(key: K): Flow<V>? {
        return flows[key]
    }

    fun getOrInitialize(key: K, initialize: suspend FlowCollector<V>.() -> Unit): Flow<V> {
        return flows.getOrPut(key) {
            cacheSharedFlow<V>().apply {
                externalScope.launch { initialize() }
                externalScope.launch { removeOnUnsubscribe(key) }
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

    fun remove(key: K) {
        flows.remove(key)
    }

    private suspend fun MutableSharedFlow<V>.removeOnUnsubscribe(key: K) {
        removeOnUnsubscribe(removalDelay) { remove(key) }
    }

    data class Entry<out K, out V>(
        val key: K,
        val value: V,
    )
}

class SingleCache<V>(
    private val externalScope: CoroutineScope,
    private val removalDelay: Duration = 1.minutes,
) : FlowCollector<V> {
    private var flow: MutableSharedFlow<V>? = null

    fun getOrInitialize(initialize: suspend FlowCollector<V>.() -> Unit): Flow<V> {
        val flow = flow ?: cacheSharedFlow<V>().apply {
            flow = this
            externalScope.launch { initialize() }
            externalScope.launch { removeOnUnsubscribe() }
        }
        return flow
            .asSharedFlow()
            .distinctUntilChanged()
    }

    override suspend fun emit(value: V) {
        flow?.emit(value)
    }

    fun remove() {
        flow = null
    }

    private suspend fun MutableSharedFlow<V>.removeOnUnsubscribe() {
        removeOnUnsubscribe(removalDelay) { remove() }
    }
}

private fun <V> cacheSharedFlow() = MutableSharedFlow<V>(replay = 1, extraBufferCapacity = 16)

@OptIn(FlowPreview::class)
private suspend fun <V> MutableSharedFlow<V>.removeOnUnsubscribe(
    delay: Duration,
    remove: () -> Unit,
) {
    var subscribedOn = false
    subscriptionCount
        .debounce { if (subscribedOn) delay else Duration.ZERO }
        .collect {
            when {
                subscribedOn && it == 0 -> remove()
                it > 0 -> subscribedOn = true
            }
        }
}