package com.thebrownfoxx.neon.client.websocket

import com.thebrownfoxx.neon.common.websocket.WebSocketSession
import com.thebrownfoxx.neon.common.websocket.incomingInstancesOf
import com.thebrownfoxx.neon.common.websocket.model.RequestId
import com.thebrownfoxx.neon.common.websocket.model.Type
import com.thebrownfoxx.neon.common.websocket.model.WebSocketMessage
import com.thebrownfoxx.neon.common.websocket.model.typeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

interface WebSocketSubscriber {
    fun <R> subscribeAsFlow(
        request: WebSocketMessage?,
        requestType: Type,
        handleResponse: SubscriptionHandler<R>.() -> Unit,
    ): Flow<R>
}

inline fun <reified T : WebSocketMessage, R> WebSocketSubscriber.subscribeAsFlow(
    request: T,
    noinline handleResponse: SubscriptionHandler<R>.() -> Unit,
): Flow<R> {
    return subscribeAsFlow(request, typeOf<T>(), handleResponse)
}

class SubscriptionHandler<R> private constructor(
    @PublishedApi internal val requestId: RequestId?,
    @PublishedApi internal val session: WebSocketSession,
    @PublishedApi internal val externalScope: CoroutineScope,
) {
    companion object {
        fun <R> create(
            requestId: RequestId?,
            session: WebSocketSession,
            externalScope: CoroutineScope,
            handleResponse: SubscriptionHandler<R>.() -> Unit,
        ) = SubscriptionHandler<R>(requestId, session, externalScope).apply { handleResponse() }
    }

    @PublishedApi
    internal val mutableResponse = MutableSharedFlow<R>()
    val response = mutableResponse.asSharedFlow()

    @PublishedApi
    internal val mutableReceivedFirst = MutableStateFlow(false)
    val receivedFirst = mutableReceivedFirst.asStateFlow()

    inline fun <reified T : WebSocketMessage> map(
        crossinline function: suspend (T) -> R,
    ) {
        externalScope.launch {
            session.incomingInstancesOf<T>().collectIndexed { index, message ->
                if (requestId != message.requestId) return@collectIndexed
                mutableResponse.emit(function(message))
                if (index == 0) mutableReceivedFirst.value = true
            }
        }
    }

    suspend fun awaitFirst() {
        receivedFirst.first { it }
    }
}