package com.thebrownfoxx.neon.client.websocket

import com.thebrownfoxx.neon.common.websocket.WebSocketSession
import com.thebrownfoxx.neon.common.websocket.incomingInstancesOf
import com.thebrownfoxx.neon.common.websocket.model.WebSocketMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ResponseHandler private constructor(
    @PublishedApi internal val coroutineScope: CoroutineScope,
    @PublishedApi internal val session: WebSocketSession,
) {
    companion object {
        fun create(
            coroutineScope: CoroutineScope,
            webSocketSession: WebSocketSession,
            handleResponse: ResponseHandler.() -> Unit,
        ) = ResponseHandler(coroutineScope, webSocketSession).apply { handleResponse() }
    }

    @PublishedApi
    internal val firstReceived = MutableStateFlow(false)

    inline fun <reified T : WebSocketMessage> onReceive(
        crossinline action: (T) -> Unit,
    ) {
        coroutineScope.launch {
            session.incomingInstancesOf<T>().collect { message ->
                action(message)
                if (!firstReceived.value) firstReceived.value = true
            }
        }
    }

    suspend fun awaitFirstReceived() {
        firstReceived.first { it }
    }
}