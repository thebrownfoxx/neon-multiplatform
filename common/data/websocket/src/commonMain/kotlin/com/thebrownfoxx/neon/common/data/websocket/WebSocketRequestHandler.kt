package com.thebrownfoxx.neon.common.data.websocket

import com.thebrownfoxx.neon.common.data.RequestHandler
import com.thebrownfoxx.neon.common.data.websocket.model.WebSocketMessage
import com.thebrownfoxx.neon.common.type.Type
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class WebSocketRequestHandler<R> private constructor(
    @PublishedApi internal val session: WebSocketSession,
    @PublishedApi internal val externalScope: CoroutineScope,
) : RequestHandler<WebSocketMessage, R> {
    @PublishedApi
    internal val jobs = mutableListOf<Job>()

    companion object{
        fun <R> create(
            webSocketSession: WebSocketSession,
            externalScope: CoroutineScope,
            handleResponse: WebSocketRequestHandler<R>.() -> Unit,
        ) = WebSocketRequestHandler<R>(webSocketSession, externalScope).apply(handleResponse)
    }

    @PublishedApi
    internal val value = MutableSharedFlow<R>(replay = 1)

    override fun map(type: Type, function: suspend (WebSocketMessage) -> R) {
        val job = externalScope.launch {
            session.incomingInstancesOf(type).collect { message ->
                value.emit(function(message))
                jobs.forEach { it.cancel() }
            }
        }
        jobs.add(job)
    }

    override suspend fun await(): R {
        return value.first()
    }
}