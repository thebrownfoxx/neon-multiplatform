package com.thebrownfoxx.neon.client.websocket

import com.thebrownfoxx.neon.client.websocket.WebSocketRequester.RequestTimeout
import com.thebrownfoxx.neon.common.websocket.WebSocketSession
import com.thebrownfoxx.neon.common.websocket.incomingInstancesOf
import com.thebrownfoxx.neon.common.websocket.model.Type
import com.thebrownfoxx.neon.common.websocket.model.WebSocketMessage
import com.thebrownfoxx.neon.common.websocket.model.typeOf
import com.thebrownfoxx.outcome.Outcome
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

interface WebSocketRequester {
    suspend fun <R> request(
        request: Any?,
        requestType: Type,
        handleResponse: RequestHandler<R>.() -> Unit,
    ): Outcome<R, RequestTimeout>

    data object RequestTimeout
}

suspend inline fun <reified T : WebSocketMessage, R> WebSocketRequester.request(
    request: T,
    noinline handleResponse: RequestHandler<R>.() -> Unit,
): Outcome<R, RequestTimeout> {
    return request(request, typeOf<T>(), handleResponse)
}

class RequestHandler<R> private constructor(
    @PublishedApi internal val session: WebSocketSession,
    @PublishedApi internal val externalScope: CoroutineScope,
) {
    @PublishedApi
    internal val jobs = mutableListOf<Job>()

    companion object {
        fun <R> create(
            webSocketSession: WebSocketSession,
            externalScope: CoroutineScope,
            handleResponse: RequestHandler<R>.() -> Unit,
        ) = RequestHandler<R>(webSocketSession, externalScope).apply { handleResponse() }
    }

    @PublishedApi
    internal val value = MutableSharedFlow<R>(replay = 1)

    inline fun <reified T : WebSocketMessage> map(
        crossinline function: suspend (T) -> R,
    ) {
        val job = externalScope.launch {
            session.incomingInstancesOf<T>().collect { message ->
                value.emit(function(message))
                jobs.forEach { it.cancel() }
            }
        }
        jobs.add(job)
    }

    suspend fun await(): R {
        return value.first()
    }
}