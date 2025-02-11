package com.thebrownfoxx.neon.common.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

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
    private val delay: Duration = 1.minutes,
) : EvictionStrategy<MutableSharedFlow<V>> {
    override suspend fun Evictor.initializeEvictor(value: MutableSharedFlow<V>) {
        onUnsubscribe(value.subscriptionCount, delay) { evict() }
    }
}

@OptIn(FlowPreview::class)
suspend fun onUnsubscribe(
    subscriptionCount: StateFlow<Int>,
    delay: Duration = 1.minutes,
    action: () -> Unit,
) {
    var subscribedOn = false
    subscriptionCount
        .debounce { if (subscribedOn) delay else Duration.ZERO }
        .collect {
            when {
                subscribedOn && it == 0 -> action()
                it > 0 -> subscribedOn = true
            }
        }
}