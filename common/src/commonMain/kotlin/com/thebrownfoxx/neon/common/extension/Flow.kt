package com.thebrownfoxx.neon.common.extension

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlin.experimental.ExperimentalTypeInference

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

@OptIn(ExperimentalTypeInference::class)
fun <T> channelFlow(@BuilderInference block: suspend ChannelFlowCollector<T>.() -> Unit): Flow<T> =
    kotlinx.coroutines.flow.channelFlow {
        ChannelFlowCollector(this).block()
    }

class ChannelFlowCollector<T>(
    private val producerScope: ProducerScope<T>,
) : CoroutineScope by producerScope, FlowCollector<T> {
    override suspend fun emit(value: T) {
        producerScope.send(value)
    }
}

fun <TL, TR, R> mergeTransform(
    leftFlow: Flow<TL>,
    rightFlow: Flow<TR>,
    transformLeft: suspend FlowCollector<R>.(TL) -> Unit,
    transformRight: suspend FlowCollector<R>.(TR) -> Unit,
): Flow<R> {
    return channelFlow {
        launch {
            leftFlow.collect {
                transformLeft(it)
            }
        }
        launch {
            rightFlow.collect {
                transformRight(it)
            }
        }
    }
}

fun <T> mergeTransform(
    leftFlow: Flow<T>,
    rightFlow: Flow<T>,
    transform: suspend FlowCollector<T>.(T) -> Unit,
): Flow<T> {
    return mergeTransform(
        leftFlow = leftFlow,
        rightFlow = rightFlow,
        transformLeft = transform,
        transformRight = transform,
    )
}