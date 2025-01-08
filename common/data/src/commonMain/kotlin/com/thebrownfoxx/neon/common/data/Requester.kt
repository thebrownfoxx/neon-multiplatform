package com.thebrownfoxx.neon.common.data

import com.thebrownfoxx.neon.common.data.Requester.RequestTimeout
import com.thebrownfoxx.neon.common.type.Type
import com.thebrownfoxx.neon.common.type.typeOf
import com.thebrownfoxx.outcome.Outcome

interface Requester<UT> {
    suspend fun <R> request(
        request: UT?,
        requestType: Type,
        handleResponse: RequestHandler<UT, R>.() -> Unit,
    ): Outcome<R, RequestTimeout>

    data object RequestTimeout
}

suspend inline fun <UT, reified T : UT, R> Requester<UT>.request(
    request: T,
    noinline handleResponse: RequestHandler<UT, R>.() -> Unit,
): Outcome<R, RequestTimeout> {
    return request(request, typeOf<T>(), handleResponse)
}

interface RequestHandler<UT, R> {
    fun map(type: Type, function: suspend (UT) -> R)
    suspend fun await(): R
}

inline fun <reified T, R> RequestHandler<in T, R>.map(
    crossinline function: suspend (T) -> R,
) {
    map(typeOf<T>()) {
        function(it as T)
    }
}