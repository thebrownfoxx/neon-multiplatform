package com.thebrownfoxx.neon.common.extension

import com.thebrownfoxx.outcome.Failure
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.Success
import com.thebrownfoxx.outcome.map.FailureMapScope
import com.thebrownfoxx.outcome.map.mapError
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

fun <T, E, RT, RE> List<Outcome<T, E>>.flatMap(
    onSuccess: (List<T>) -> RT,
    onFailure: (E) -> RE,
): Outcome<RT, RE> {
    return when {
        all { it is Success } -> {
            val values = filterIsInstance<Success<T>>().map { it.value }
            Success(onSuccess(values))
        }
        else -> {
            val firstFailure = filterIsInstance<Failure<E>>().first()
            firstFailure.mapError { onFailure(it) }
        }
    }
}

fun <T, E, RT> List<Outcome<T, E>>.flatMap(
    onSuccess: (List<T>) -> RT,
): Outcome<RT, E> {
    return flatMap(
        onSuccess = onSuccess,
        onFailure = { it },
    )
}

fun <T, E, RE> List<Outcome<T, E>>.flatMapError(
    onFailure: (E) -> RE,
): Outcome<List<T>, RE> {
    return flatMap(
        onSuccess = { it },
        onFailure = onFailure,
    )
}