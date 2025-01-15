package com.thebrownfoxx.neon.common.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

class ReactiveCache<in K, out V>(
    scope: CoroutineScope,
    private val get: suspend (K) -> V,
) {
    private val cache = Cache<K, V>(scope)

    fun getAsFlow(key: K): Flow<V> {
        return cache.getAsFlow(key) {
            emit(get(key))
        }
    }

    suspend fun update(key: K) {
        cache.emit(key, get(key))
    }
}

class SingleReactiveCache<out V>(
    scope: CoroutineScope,
    private val get: suspend () -> V,
) {
    private val cache = SingleCache<V>(scope)

    fun getAsFlow(): Flow<V> {
        return cache.getAsFlow {
            emit(get())
        }
    }

    suspend fun update() {
        cache.emit(get())
    }
}