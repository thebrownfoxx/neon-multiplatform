package com.thebrownfoxx.neon.common.data.transaction

class Reversible<out T>(
    internal val result: T,
    private val onFinalize: suspend () -> Unit = {},
    val reverse: suspend () -> Unit,
) {
    suspend fun finalize(): T {
        onFinalize()
        return result
    }
}

fun <T> Reversible<T>.onFinalize(block: suspend () -> Unit) = Reversible(result, block, reverse)