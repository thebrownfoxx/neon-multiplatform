package com.thebrownfoxx.neon.common.extension

import com.thebrownfoxx.outcome.Failure
import com.thebrownfoxx.outcome.Outcome

fun <E> Outcome<*, E>.failedWith(predicate: (E) -> Boolean): Boolean {
    return this is Failure && predicate(error)
}

fun <E> Outcome<*, E>.failedWith(vararg matches: E): Boolean {
    return matches.any { match -> failedWith { it == match } }
}