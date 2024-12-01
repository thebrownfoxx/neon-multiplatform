package com.thebrownfoxx.neon.common.data

import kotlinx.coroutines.CoroutineScope

class ReactiveCache<in K, out V>(
    scope: CoroutineScope,
    private val gettable: Gettable<K, V>,
) {
    private val cache = Cache<K, V>(scope)

    fun getAsFlow(key: K) = cache.getAsFlow(key) {
        emit(gettable.get(key))
    }

    suspend fun updateCache(key: K) {
        cache.emit(key, gettable.get(key))
    }
}