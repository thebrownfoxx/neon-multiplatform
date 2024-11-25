package com.thebrownfoxx.neon.common.type

sealed interface Outcome<out T, out E>

data class Success<out T>(val value: T) : Outcome<T, Nothing>

data class Failure<out E>(val error: E) : Outcome<Nothing, E>

inline fun <R, T, E> Outcome<T, E>.fold(onSuccess: (T) -> R, onFailure: (E) -> R): R {
    return when (this) {
        is Success -> onSuccess(value)
        is Failure -> onFailure(error)
    }
}

inline fun <R, T: R, E> Outcome<T, E>.getOrElse(onFailure: (E) -> R): R {
    return when (this) {
        is Success -> value
        is Failure -> onFailure(error)
    }
}

fun <T, E> Outcome<T, E>.getOrNull(): T? {
    return getOrElse { null }
}

fun <T, E> Outcome<T, E>.get(): T {
    return getOrElse {
        throw IllegalArgumentException("Cannot get value from failure outcome $this")
    }
}

inline fun <R, T, E> Outcome<T, E>.map(transform: (T) -> R): Outcome<R, E> {
    return when (this) {
        is Success -> Success(transform(value))
        is Failure -> this
    }
}

inline fun <RT, RE, T, E> Outcome<T, E>.map(
    onSuccess: (T) -> RT,
    onFailure: (E) -> RE,
): Outcome<RT, RE> {
    return when (this) {
        is Success -> Success(onSuccess(value))
        is Failure -> Failure(onFailure(error))
    }
}

inline fun <RE, T, E> Outcome<T, E>.mapError(onFailure: (E) -> RE): Outcome<T, RE> {
    return when (this) {
        is Success -> Success(value)
        is Failure -> Failure(onFailure(error))
    }
}

inline fun <T, E> Outcome<T, E>.onSuccess(function: (T) -> Unit): Outcome<T, E> {
    if (this is Success) function(value)
    return this
}

inline fun <T, E> Outcome<T, E>.onFailure(function: (E) -> Unit): Outcome<T, E> {
    if (this is Failure) function(error)
    return this
}

inline fun <T> runFailing(function: () -> T): Outcome<T, Exception> {
    return try {
        Success(function())
    } catch (e: Exception) {
        Failure(e)
    }
}

typealias UnitOutcome<E> = Outcome<Unit, E>

typealias UnitSuccess = Success<Unit>

fun unitSuccess() = Success(Unit)

fun <T> T.asSuccess() = Success(this)

fun <E> E.asFailure() = Failure(this)