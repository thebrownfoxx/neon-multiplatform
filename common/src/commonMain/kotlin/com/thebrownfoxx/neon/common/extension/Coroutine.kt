package com.thebrownfoxx.neon.common.extension

import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.runFailing
import kotlinx.coroutines.CoroutineScope
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
suspend fun <R> coroutineScope(block: suspend CoroutineScope.() -> R): Outcome<R, Exception> {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return runFailing { kotlinx.coroutines.coroutineScope(block) }
}

@OptIn(ExperimentalContracts::class)
suspend fun <R> supervisorScope(block: suspend CoroutineScope.() -> R): Outcome<R, Exception> {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return runFailing { kotlinx.coroutines.supervisorScope(block) }
}