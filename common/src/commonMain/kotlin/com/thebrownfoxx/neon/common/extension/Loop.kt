package com.thebrownfoxx.neon.common.extension

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
inline fun loop(block: LoopScope.() -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.UNKNOWN)
    }

    val scope = LoopScope()
    while (!scope.canceled) {
        scope.block()
    }
}

class LoopScope @PublishedApi internal constructor() {
    @PublishedApi
    internal var canceled = false

    fun breakLoop() {
        canceled = true
    }
}