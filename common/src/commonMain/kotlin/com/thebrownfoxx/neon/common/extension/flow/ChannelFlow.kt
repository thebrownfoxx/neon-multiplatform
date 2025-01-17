package com.thebrownfoxx.neon.common.extension.flow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlin.experimental.ExperimentalTypeInference

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