package com.thebrownfoxx.neon.common.extension

import com.thebrownfoxx.outcome.Failure
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.Success
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.supervisorScope
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
suspend fun <R> coroutineScope(block: suspend CoroutineScope.() -> R): Outcome<R, Exception> {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }
    return try {
        Success(coroutineScope(block))
    } catch (e: Exception) {
        Failure(e)
    }
}

@OptIn(ExperimentalContracts::class)
suspend fun <R> supervisorScope(block: suspend CoroutineScope.() -> R): Outcome<R, Exception> {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }
    return try {
        Success(supervisorScope(block))
    } catch (e: Exception) {
        Failure(e)
    }
}