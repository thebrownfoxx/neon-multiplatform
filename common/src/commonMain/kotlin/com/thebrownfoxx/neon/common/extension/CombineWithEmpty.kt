package com.thebrownfoxx.neon.common.extension

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow

/**
 * `kotlinx.coroutines.flow.combine` doesn't emit anything if the input Iterable is empty.
 * This returns a flow that emits the transformation of an empty array if the input
 * is an empty Collection.
 */
inline fun <reified T, R> combineOrEmpty(
    flows: Collection<Flow<T>>,
    crossinline transform: suspend (Array<T>) -> R,
): Flow<R> {
    return when {
        flows.isEmpty() -> flow { emit(transform(emptyArray())) }
        else -> combine(flows) { transform(it) }
    }
}