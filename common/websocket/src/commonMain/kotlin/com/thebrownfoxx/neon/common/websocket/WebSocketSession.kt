package com.thebrownfoxx.neon.common.websocket

import com.thebrownfoxx.neon.common.websocket.model.SerializedWebSocketMessage
import com.thebrownfoxx.neon.common.websocket.model.Type
import com.thebrownfoxx.neon.common.websocket.model.WebSocketMessage
import com.thebrownfoxx.neon.common.websocket.model.WebSocketMessageLabel
import com.thebrownfoxx.neon.common.websocket.model.deserialize
import com.thebrownfoxx.neon.common.websocket.model.typeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

abstract class WebSocketSession {
    abstract val sessionScope: CoroutineScope
    abstract val close: Flow<Unit>
    abstract val incomingMessages: Flow<SerializedWebSocketMessage>
    abstract suspend fun send(message: Any?, type: Type)
    abstract suspend fun close()

    suspend inline fun <reified T : WebSocketMessage> send(message: T) {
        send(message, typeOf<T>())
    }

    inline fun <reified T : WebSocketMessage> subscribe(
        crossinline action: (T) -> Unit,
    ): Job {
        return sessionScope.launch {
            incomingMessages
                .filter { it.getLabel() == WebSocketMessageLabel(T::class) }
                .collect { serializedMessage ->
                    action(serializedMessage.deserialize<T>())
                }
        }
    }
}