package com.thebrownfoxx.neon.common.extension

import com.thebrownfoxx.outcome.Failure
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.map.FailureMapScope
import com.thebrownfoxx.outcome.map.onFailure

fun <E> Outcome<*, E>.failedWith(predicate: (E) -> Boolean): Boolean {
    return this is Failure && predicate(error)
}

fun <E> Outcome<*, E>.failedWith(vararg matches: E): Boolean {
    return matches.any { match -> failedWith { it == match } }
}

inline fun <T, E> List<Outcome<T, E>>.onFailure(
    function: FailureMapScope.(E) -> Unit,
): List<Outcome<T, E>> {
    return map { it.onFailure(function) }
}