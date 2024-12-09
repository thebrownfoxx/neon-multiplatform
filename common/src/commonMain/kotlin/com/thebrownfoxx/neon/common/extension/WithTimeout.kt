package com.thebrownfoxx.neon.common.extension

import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.blockContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withTimeout
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.time.Duration

@OptIn(ExperimentalContracts::class)
suspend fun <T> withTimeout(
    timeout: Duration,
    block: suspend CoroutineScope.() -> T,
): Outcome<T, TimeoutError> {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    blockContext("withTimeout") {
        return runFailing { withTimeout(timeout.inWholeMilliseconds, block) }
            .mapError { TimeoutError }
    }
}

data object TimeoutError