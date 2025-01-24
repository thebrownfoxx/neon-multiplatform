package com.thebrownfoxx.neon.common.data.transaction

import com.thebrownfoxx.outcome.Failure
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.Success
import com.thebrownfoxx.outcome.UnitOutcome
import com.thebrownfoxx.outcome.map.mapError

typealias ReversibleOutcome<T, E> = Reversible<Outcome<T, E>>

typealias ReversibleUnitOutcome<E> = Reversible<UnitOutcome<E>>

fun <T, E> Outcome<T, E>.asReversible(reverse: suspend () -> Unit) = when (this) {
    is Success -> Reversible(result = this, reverse = reverse)
    is Failure -> asReversible()
}

fun <E> Failure<E>.asReversible() = Reversible(this) {}

fun <T, E, RT, RE> List<ReversibleOutcome<T, E>>.flatMap(
    onSuccess:  (List<T>) -> RT,
    onFailure: (E) -> RE,
): ReversibleOutcome<RT, RE> {
    return when {
        all { it.result is Success } -> {
            val values = map { (it.result as Success).value }
            Success(onSuccess(values))
        }
        else -> {
            val firstFailure = first { it.result is Failure }.result as Failure
            firstFailure.mapError { onFailure(it) }
        }
    }
        .asReversible { forEach { it.reverse() } }
        .onFinalize { forEach { it.finalize() } }
}

fun <T, E, RT> List<ReversibleOutcome<T, E>>.flatMap(
    onSuccess:  (List<T>) -> RT,
): ReversibleOutcome<RT, E> {
    return flatMap(
        onSuccess = onSuccess,
        onFailure = { it },
    )
}

fun <T, E, RE> List<ReversibleOutcome<T, E>>.flatMapError(
    onFailure:  (E) -> RE,
): ReversibleOutcome<List<T>, RE> {
    return flatMap(
        onSuccess = { it },
        onFailure = onFailure,
    )
}