package com.thebrownfoxx.neon.common.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration

class CacheMap<K, V> internal constructor(
    private val internalMap: MutableMap<K, V>,
    private val evictionStrategy: EvictionStrategy<V>,
    private val externalScope: CoroutineScope,
) : MutableMap<K, V> by internalMap {
    override fun put(key: K, value: V): V? {
        return internalMap.put(key, value).also { initializeEvictor(key, value) }
    }

    private fun initializeEvictor(key: K, value: V) {
        externalScope.launch {
            val evictor = Evictor { internalMap.remove(key) }
            with(evictionStrategy) { evictor.initializeEvictor(value) }
        }
    }
}

fun <K, V> CacheMap(
    evictionStrategy: EvictionStrategy<V>,
    externalScope: CoroutineScope,
) = CacheMap(
    internalMap = ConcurrentHashMap<K, V>(),
    evictionStrategy = evictionStrategy,
    externalScope = externalScope,
)

fun interface Evictor {
    fun evict()
}

fun interface EvictionStrategy<in V> {
    suspend fun Evictor.initializeEvictor(value: V)
}

class EvictOnUnsubscribeStrategy<V>(
    private val delay: Duration,
) : EvictionStrategy<MutableSharedFlow<V>> {
    @OptIn(FlowPreview::class)
    override suspend fun Evictor.initializeEvictor(value: MutableSharedFlow<V>) {
        var subscribedOn = false
        value.subscriptionCount
            .debounce { if (subscribedOn) delay else Duration.ZERO }
            .collect {
                when {
                    subscribedOn && it == 0 -> evict()
                    it > 0 -> subscribedOn = true
                }
            }
    }
}