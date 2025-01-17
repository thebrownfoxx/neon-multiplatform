package com.thebrownfoxx.neon.common.extension.flow

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch

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