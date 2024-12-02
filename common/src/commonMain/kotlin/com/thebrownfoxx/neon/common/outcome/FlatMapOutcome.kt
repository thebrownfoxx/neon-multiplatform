package com.thebrownfoxx.neon.common.outcome

inline fun <RT, RE, T, EI, EO> Outcome<Outcome<T, EI>, EO>.flatMap(
    onSuccess: (T) -> RT,
    onInnerFailure: (EI) -> RE,
    onOuterFailure: (EO) -> RE,
): Outcome<RT, RE> {
    return when (this) {
        is Success -> when (value) {
            is Success -> Success(onSuccess(value.value))
            is Failure -> Failure(onInnerFailure(value.error))
        }
        is Failure -> Failure(onOuterFailure(error))
    }
}

inline fun <RT, RE, T, EI, EO> Outcome<Outcome<T, EI>, EO>.flatMap(
    onSuccess: (T) -> RT,
    onFailure: (FlatMapFailure<EI, EO>) -> RE
): Outcome<RT, RE> {
    return when (this) {
        is Success -> when (value) {
            is Success -> Success(onSuccess(value.value))
            is Failure -> Failure(onFailure(FlatMapFailure.Inner(value.error)))
        }
        is Failure -> Failure(onFailure(FlatMapFailure.Outer(error)))
    }
}

inline fun <RE, T, EI, EO> Outcome<Outcome<T, EI>, EO>.flatMapError(
    onInnerFailure: (EI) -> RE,
    onOuterFailure: (EO) -> RE,
): Outcome<T, RE> {
    return when (this) {
        is Success -> when (value) {
            is Success -> value
            is Failure -> Failure(onInnerFailure(value.error))
        }
        is Failure -> Failure(onOuterFailure(error))
    }
}

inline fun <RE, T, EI, EO> Outcome<Outcome<T, EI>, EO>.flatMapError(
    onFailure: (FlatMapFailure<EI, EO>) -> RE,
): Outcome<T, RE> {
    return when (this) {
        is Success -> when (value) {
            is Success -> value
            is Failure -> Failure(onFailure(FlatMapFailure.Inner(value.error)))
        }
        is Failure -> Failure(onFailure(FlatMapFailure.Outer(error)))
    }
}

inline fun <T, EI, EO> Outcome<Outcome<T, EI>, EO>.onInnerSuccess(
    function: (T) -> Unit,
): Outcome<Outcome<T, EI>, EO> {
    if (this is Success && value is Success) function(value.value)
    return this
}

inline fun <T, EI, EO> Outcome<Outcome<T, EI>, EO>.onInnerFailure(
    function: (EI) -> Unit,
): Outcome<Outcome<T, EI>, EO> {
    if (this is Success && value is Failure) function(value.error)
    return this
}

inline fun <T, EI, EO> Outcome<Outcome<T, EI>, EO>.onOuterFailure(
    function: (EO) -> Unit,
): Outcome<Outcome<T, EI>, EO> {
    if (this is Failure) function(error)
    return this
}

inline fun <T, EI, EO> Outcome<Outcome<T, EI>, EO>.onAnyFailure(
    function: (FlatMapFailure<EI, EO>) -> Unit,
): Outcome<Outcome<T, EI>, EO> {
    if (this is Success && value is Failure) function(FlatMapFailure.Inner(value.error))
    if (this is Failure) function(FlatMapFailure.Outer(error))
    return this
}

sealed interface FlatMapFailure<out EI, out EO> {
    data class Inner<out EI>(val error: EI) : FlatMapFailure<EI, Nothing>
    data class Outer<out EO>(val error: EO) : FlatMapFailure<Nothing, EO>
}