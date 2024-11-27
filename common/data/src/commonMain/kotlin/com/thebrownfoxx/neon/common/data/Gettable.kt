package com.thebrownfoxx.neon.common.data

import com.thebrownfoxx.neon.common.type.Outcome
import com.thebrownfoxx.neon.common.type.id.Id

fun interface Gettable<in K : Id, out V> {
    suspend fun get(id: K): Outcome<V, GetError>
}