package com.thebrownfoxx.neon.common.extension

import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.map.mapError
import com.thebrownfoxx.outcome.runFailing
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration

suspend fun <T> withTimeout(
    timeout: Duration,
    block: suspend WithTimeoutScope.() -> T,
): Outcome<T, Timeout> {
    val scope = WithTimeoutScope()
    return runFailing { withTimeout(timeout.inWholeMilliseconds) { scope.block() } }
        .mapError { Timeout }
        .also { scope.afterTimeout?.invoke() }
}

data object Timeout

class WithTimeoutScope internal constructor() {
    internal var afterTimeout: (suspend () -> Unit)? = null

    fun runAfterTimeout(function: suspend () -> Unit) {
        afterTimeout = function
    }
}