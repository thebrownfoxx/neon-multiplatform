package com.thebrownfoxx.neon.common.extension

import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flowOf

fun <T> T.flow() = flowOf(this)

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

suspend fun <T, R> ProducerScope<R>.mirror(
    source: Flow<T>,
    transform: suspend (T) -> R,
) {
    source.collect { send(transform(it)) }
}

suspend fun <R> ProducerScope<R>.mirror(source: Flow<R>) {
    mirror(source) { it }
}

suspend fun <T, R> Flow<T>.mirrorTo(
    destination: ProducerScope<R>,
    transform: suspend (T) -> R,
) {
    destination.mirror(this, transform)
}

suspend fun <R> Flow<R>.mirrorTo(destination: ProducerScope<R>) {
    mirrorTo(destination) { it }
}