package com.thebrownfoxx.neon.common.extension.flow

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector

suspend fun <T, R> FlowCollector<R>.mirror(
    source: Flow<T>,
    transform: suspend (T) -> R,
) {
    source.collect { emit(transform(it)) }
}

suspend fun <R> FlowCollector<R>.mirror(source: Flow<R>) {
    mirror(source) { it }
}

suspend fun <T, R> Flow<T>.mirrorTo(
    destination: FlowCollector<R>,
    transform: suspend (T) -> R,
) {
    destination.mirror(this, transform)
}

suspend fun <R> Flow<R>.mirrorTo(destination: FlowCollector<R>) {
    mirrorTo(destination) { it }
}