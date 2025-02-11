package com.thebrownfoxx.neon.common.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class ReactiveCache<in K, out V>(
    private val externalScope: CoroutineScope,
    private val get: suspend (K) -> V,
) {
    private val cache = flowCacheMap<K, V>(externalScope)

    fun getAsFlow(key: K): Flow<V> {
        return cache.getOrPut(key) {
            cacheFlow<V>().apply { emitValue(key) }
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
    externalScope: CoroutineScope,
    private val get: suspend () -> V,
) {
    private val cache: MutableSharedFlow<V> = MutableSharedFlow(replay = 1)

    init {
        externalScope.launch { cache.emit(get()) }
    }

    fun getAsFlow(): Flow<V> {
        return cache
    }

    suspend fun update() {
        cache.emit(get())
    }
}