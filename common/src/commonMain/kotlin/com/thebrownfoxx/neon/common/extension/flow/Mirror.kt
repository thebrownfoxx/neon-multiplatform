package com.thebrownfoxx.neon.common.extension.flow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch

suspend fun <T, R> FlowCollector<R>.mirror(
    source: Flow<T>,
    transform: suspend (T) -> R,
) {
    source.collect { emit(transform(it)) }
}

fun <T, R> FlowCollector<R>.mirror(
    scope: CoroutineScope,
    source: Flow<T>,
    transform: suspend (T) -> R,
) {
    scope.launch {
        mirror(source, transform)
    }
}

suspend fun <R> FlowCollector<R>.mirror(source: Flow<R>) {
    mirror(source) { it }
}

fun <R> FlowCollector<R>.mirror(
    scope: CoroutineScope,
    source: Flow<R>,
) {
    scope.launch {
        mirror(source)
    }
}

suspend fun <T, R> Flow<T>.mirrorTo(
    destination: FlowCollector<R>,
    transform: suspend (T) -> R,
) {
    destination.mirror(this, transform)
}

fun <T, R> Flow<T>.mirrorTo(
    scope: CoroutineScope,
    destination: FlowCollector<R>,
    transform: suspend (T) -> R,
) {
    scope.launch {
        mirrorTo(destination, transform)
    }
}

suspend fun <R> Flow<R>.mirrorTo(destination: FlowCollector<R>) {
    mirrorTo(destination) { it }
}

fun <R> Flow<R>.mirrorTo(
    scope: CoroutineScope,
    destination: FlowCollector<R>,
) {
    scope.launch {
        mirrorTo(destination)
    }
}