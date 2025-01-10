package com.thebrownfoxx.neon.common.extension

import com.thebrownfoxx.outcome.Outcome
import kotlinx.coroutines.delay
import kotlin.math.pow
import kotlin.time.Duration

data class ExponentialBackoffValues(
    val initialDelay: Duration,
    val maxDelay: Duration,
    val factor: Double,
)

class ExponentialBackoff(values: ExponentialBackoffValues) {
    constructor(
        initialDelay: Duration,
        maxDelay: Duration,
        factor: Double,
    ) : this(ExponentialBackoffValues(initialDelay, maxDelay, factor))

    private val initialDelay = values.initialDelay
    private val maxDelay = values.maxDelay
    private val factor = values.factor

    private var retryCount = 0

    private fun getNextDelay(): Duration =
        minOf(maxDelay, initialDelay * factor.pow(retryCount)).also { retryCount++ }

    /**
     * Delays the coroutine and increments the retry count.
     */
    suspend fun delay() = delay(getNextDelay())

    /**
     * Runs the coroutine inside with a timeout and increments the retry count.
     */
    suspend fun <T> withTimeout(block: suspend WithTimeoutScope.() -> T): Outcome<T, Timeout> {
        return withTimeout(getNextDelay(), block)
    }

    fun reset() {
        retryCount = 0
    }

    override fun toString() =
        "ExponentialBackoff(initialDelay=$initialDelay, maxDelay=$maxDelay, factor=$factor)"
}