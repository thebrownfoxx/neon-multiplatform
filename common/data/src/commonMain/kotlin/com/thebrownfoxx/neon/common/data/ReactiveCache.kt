package com.thebrownfoxx.neon.common.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.minutes

class ReactiveCache<in K, out V>(
    private val externalScope: CoroutineScope,
    private val get: suspend (K) -> V,
) {
    private val cache = CacheMap<K, MutableSharedFlow<V>>(
        EvictOnUnsubscribeStrategy(1.minutes),
        externalScope,
    )

    fun getAsFlow(key: K): Flow<V> {
        return cache.getOrPut(key) {
            cacheSharedFlow<V>().apply { emitValue(key) }
        }
    }

    suspend fun update(key: K) {
        cache[key]?.emit(get(key))
    }

    private fun MutableSharedFlow<V>.emitValue(key: K) {
        externalScope.launch { emit(get(key)) }
    }
}

class SingleReactiveCache<out V>(
    private val externalScope: CoroutineScope,
    private val get: suspend () -> V,
) {
    private val cache: MutableSharedFlow<V>? = null

    fun getAsFlow(): Flow<V> {
        return cache ?: cacheSharedFlow()
    }

    suspend fun update() {
        cache?.emit(get())
    }

    private fun MutableSharedFlow<V>.emitValue() {
        externalScope.launch { emit(get()) }
    }
}