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
import kotlinx.coroutines.launch

interface WebSocketSession {
    val close: Flow<Unit>
    val incomingMessages: Flow<SerializedWebSocketMessage>
    suspend fun send(message: Any?, type: Type)
    suspend fun close()
}

suspend inline fun <reified T : WebSocketMessage> WebSocketSession.send(message: T) {
    send(message, typeOf<T>())
}

inline fun <reified T : WebSocketMessage> WebSocketSession.subscribe(
    scope: CoroutineScope,
    label: WebSocketMessageLabel,
    crossinline action: (T) -> Unit,
): Job {
    return scope.launch {
        incomingMessages.collect { serializedMessage ->
            if (serializedMessage.getLabel() == label) {
                action(serializedMessage.deserialize<T>())
            }
        }
    }
}