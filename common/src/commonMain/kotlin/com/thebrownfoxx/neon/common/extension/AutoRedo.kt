package com.thebrownfoxx.neon.common.extension

import com.thebrownfoxx.outcome.Outcome
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlin.math.pow
import kotlin.time.Duration

class ExponentialBackoff(
    val initialDelay: Duration,
    val maxDelay: Duration,
    val factor: Double,
) {
    private var retryCount = 0

    private fun getNextDelay(): Duration =
        minOf(maxDelay, initialDelay * factor.pow(retryCount)).also { retryCount++ }

    suspend fun delay() = delay(getNextDelay())

    suspend fun <T> withTimeout(block: suspend CoroutineScope.() -> T): Outcome<T, TimeoutError> {
        return withTimeout(getNextDelay(), block)
    }

    fun reset() {
        retryCount = 0
    }

    override fun toString() =
        "ExponentialBackoff(initialDelay=$initialDelay, maxDelay=$maxDelay, factor=$factor)"
}