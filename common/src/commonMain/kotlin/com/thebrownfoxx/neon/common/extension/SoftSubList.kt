package com.thebrownfoxx.neon.common.extension

fun <T> List<T>.softSubList(range: IntRange): List<T> {
    val start = range.first.coerceAtLeast(0)
    val end = range.endInclusive.coerceAtMost(lastIndex)
    return subList(start, end + 1)
}