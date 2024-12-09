package com.thebrownfoxx.neon.common.data.transaction

interface Reversible<out T> {
    val result: T
    suspend fun finalize()
    suspend fun reverse()
}

fun <T> Reversible(
    result: T,
    finalize: suspend () -> Unit = {},
    reverse: suspend () -> Unit,
) = object : Reversible<T> {
    override val result = result
    override suspend fun finalize() = finalize()
    override suspend fun reverse() = reverse()
}