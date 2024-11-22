package com.thebrownfoxx.neon.common.model

inline fun <T> runFailing(function: () -> T): Result<T, Exception> {
    return try {
        Success(function())
    } catch (e: Exception) {
        Failure(e)
    }
}

typealias UnitResult<E> = Result<Unit, E>

typealias UnitSuccess = Success<Unit>

fun unitSuccess() = Success(Unit)

sealed interface Result<out T, out E>

data class Success<out T>(val value: T) : Result<T, Nothing>

data class Failure<out E>(val error: E) : Result<Nothing, E>

inline fun <R, T, E> Result<T, E>.fold(onSuccess: (T) -> R, onFailure: (E) -> R): R {
    return when (this) {
        is Success -> onSuccess(value)
        is Failure -> onFailure(error)
    }
}

inline fun <R, T: R, E> Result<T, E>.getOrElse(onFailure: (E) -> R): R {
    return when (this) {
        is Success -> value
        is Failure -> onFailure(error)
    }
}

fun <T, E> Result<T, E>.getOrNull(): T? {
    return getOrElse { null }
}

fun <T, E> Result<T, E>.get(): T {
    return getOrElse {
        throw IllegalArgumentException("Cannot get value from failure result $this")
    }
}

inline fun <R, T, E> Result<T, E>.map(transform: (T) -> R): Result<R, E> {
    return when (this) {
        is Success -> Success(transform(value))
        is Failure -> this
    }
}

inline fun <RT, RE, T, E> Result<T, E>.map(
    onSuccess: (T) -> RT,
    onFailure: (E) -> RE,
): Result<RT, RE> {
    return when (this) {
        is Success -> Success(onSuccess(value))
        is Failure -> Failure(onFailure(error))
    }
}

inline fun <RE, T, E> Result<T, E>.mapError(onFailure: (E) -> RE): Result<T, RE> {
    return when (this) {
        is Success -> Success(value)
        is Failure -> Failure(onFailure(error))
    }
}

inline fun <T, E> Result<T, E>.onSuccess(function: (T) -> Unit) {
    if (this is Success) function(value)
}

inline fun <T, E> Result<T, E>.onFailure(function: (E) -> Unit) {
    if (this is Failure) function(error)
}

fun <T> T.asSuccess() = Success(this)

fun <E> E.asFailure() = Failure(this)