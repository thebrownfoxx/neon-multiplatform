package com.thebrownfoxx.neon.common.extension.flow

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow

inline fun <reified T, R> Iterable<Flow<T>>.combine(
    crossinline transform: suspend (Array<T>) -> R
): Flow<R> {
    return combine(this, transform)
}

/**
 * `kotlinx.coroutines.flow.combine` doesn't emit anything if the input Iterable is empty.
 * This returns a flow that emits the transformation of an empty array if the input
 * is an empty Collection.
 */
@JvmName("combineFlowsOrEmpty")
inline fun <reified T, R> combineOrEmpty(
    flows: Collection<Flow<T>>,
    crossinline transform: suspend (Array<T>) -> R,
): Flow<R> {
    return when {
        flows.isEmpty() -> flow { emit(transform(emptyArray())) }
        else -> combine(flows) { transform(it) }
    }
}

/**
 * `kotlinx.coroutines.flow.combine` doesn't emit anything if the input Iterable is empty.
 * This returns a flow that emits the transformation of an empty array if the input
 * is an empty Collection.
 */
inline fun <reified T, R> Collection<Flow<T>>.combineOrEmpty(
    crossinline transform: suspend (Array<T>) -> R
): Flow<R> {
    return combineOrEmpty(this, transform)
}