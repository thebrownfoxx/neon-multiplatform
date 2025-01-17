package com.thebrownfoxx.neon.common.extension

import com.thebrownfoxx.outcome.Failure
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.Success

fun <T> Collection<T>.firstOrNotFound(predicate: (T) -> Boolean): Outcome<T, NotFound> {
    return when (val first = firstOrNull(predicate)) {
        null -> Failure(NotFound)
        else -> Success(first)
    }
}

data object NotFound