package com.thebrownfoxx.neon.common.data

import kotlinx.coroutines.CoroutineScope

class ReactiveCache<in K, out V>(
    scope: CoroutineScope,
    private val get: suspend (K) -> V,
) {
    private val cache = Cache<K, V>(scope)

    fun getAsFlow(key: K) = cache.getAsFlow(key) {
        emit(get(key))
    }

    suspend fun updateCache(key: K) {
        cache.emit(key, get(key))
    }
}

class SingleReactiveCache<out V>(
    scope: CoroutineScope,
    private val get: suspend () -> V,
) {
    private val cache = SingleCache<V>(scope)

    fun getAsFlow() = cache.getAsFlow {
        emit(get())
    }

    suspend fun updateCache() {
        cache.emit(get())
    }
}