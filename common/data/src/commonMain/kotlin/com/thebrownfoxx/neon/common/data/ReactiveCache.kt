package com.thebrownfoxx.neon.common.data

import com.thebrownfoxx.neon.common.type.Outcome
import com.thebrownfoxx.neon.common.type.id.Id
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class ReactiveCache<in K : Id, out V>(
    private val scope: CoroutineScope,
    private val gettable: Gettable<K, V>,
) {
    private val flows = HashMap<K, MutableSharedFlow<Outcome<V, GetError>>>()

    fun getAsFlow(id: K): Flow<Outcome<V, GetError>> {
        val savedFlow = flows[id]
        if (savedFlow != null) return savedFlow

        val newFlow = MutableSharedFlow<Outcome<V, GetError>>(replay = 1)
        flows[id] = newFlow
        scope.launch { updateCache(id) }
        return newFlow
    }

    suspend fun updateCache(id: K) {
        flows[id]?.emit(gettable.get(id))
    }
}