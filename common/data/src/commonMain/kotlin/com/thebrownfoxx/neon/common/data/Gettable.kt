package com.thebrownfoxx.neon.common.data

fun interface Gettable<in K, out V> {
    suspend fun get(key: K): V
}