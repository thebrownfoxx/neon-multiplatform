package com.thebrownfoxx.neon.common.outcome

sealed interface Outcome<out T, out E>

data class Success<out T>(val value: T) : Outcome<T, Nothing>

data class Failure<out E>(val error: E) : Outcome<Nothing, E>

typealias UnitOutcome<E> = Outcome<Unit, E>

fun unitSuccess() = Success(Unit)

fun <T> T.asSuccess() = Success(this)

fun <E> E.asFailure() = Failure(this)