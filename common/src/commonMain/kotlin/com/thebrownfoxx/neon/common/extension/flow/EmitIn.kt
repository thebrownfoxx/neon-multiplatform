package com.thebrownfoxx.neon.common.extension.flow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch

fun <T> FlowCollector<T>.emitIn(scope: CoroutineScope, value: T) {
    scope.launch { emit(value) }
}