package com.thebrownfoxx.neon.common.websocket

import com.thebrownfoxx.neon.common.websocket.model.SerializedWebSocketMessage
import com.thebrownfoxx.neon.common.websocket.model.Type
import com.thebrownfoxx.neon.common.websocket.model.WebSocketMessage
import com.thebrownfoxx.neon.common.websocket.model.typeOf
import kotlinx.coroutines.flow.Flow

interface WebSocketSession {
    val incomingMessages: Flow<SerializedWebSocketMessage>
    suspend fun send(message: Any?, type: Type)
    suspend fun close()
}

suspend inline fun <reified T : WebSocketMessage> WebSocketSession.send(message: T) {
    send(message, typeOf<T>())
}