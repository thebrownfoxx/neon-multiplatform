package com.thebrownfoxx.neon.common.data.transaction

interface Reversible<out T> {
    val result: T
    suspend fun reverse()
}

fun <T> Reversible(
    result: T,
    reverse: suspend () -> Unit,
) = object : Reversible<T> {
    override val result = result
    override suspend fun reverse() = reverse()
}