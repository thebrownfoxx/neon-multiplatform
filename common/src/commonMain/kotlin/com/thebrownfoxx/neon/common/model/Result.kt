package com.thebrownfoxx.neon.common.model

typealias UnitResult<E> = Result<Unit, E>

sealed interface Result<out T, out E>

fun UnitSuccess() = Success(Unit)

data class Success<out T>(val value: T) : Result<T, Nothing>

data class Failure<out E>(val error: E) : Result<Nothing, E>

fun <R, T, E> Result<T, E>.fold(onSuccess: (T) -> R, onFailure: (E) -> R): R {
    return when (this) {
        is Success -> onSuccess(value)
        is Failure -> onFailure(error)
    }
}

fun <R, T: R, E> Result<T, E>.getOrElse(onFailure: (E) -> R): R {
    return when (this) {
        is Success -> value
        is Failure -> onFailure(error)
    }
}

fun <T, E> Result<T, E>.getOrNull(): T? {
    return getOrElse { null }
}

fun <R, T, E> Result<T, E>.map(transform: (T) -> R): Result<R, E> {
    return when (this) {
        is Success -> Success(transform(value))
        is Failure -> this
    }
}

fun <RT, RE, T, E> Result<T, E>.map(
    onSuccess: (T) -> RT,
    onFailure: (E) -> RE,
): Result<RT, RE> {
    return when (this) {
        is Success -> Success(onSuccess(value))
        is Failure -> Failure(onFailure(error))
    }
}