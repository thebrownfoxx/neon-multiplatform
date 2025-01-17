package com.thebrownfoxx.neon.common.extension.flow

import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.Success
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map

fun <T> Flow<Outcome<T, *>>.filterSuccess(): Flow<T> {
    return filterIsInstance<Success<T>>()
        .map { it.value }
}