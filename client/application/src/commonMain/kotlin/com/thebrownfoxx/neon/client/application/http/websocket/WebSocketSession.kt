package com.thebrownfoxx.neon.client.application.http.websocket

import com.thebrownfoxx.neon.common.type.id.Id
import com.thebrownfoxx.neon.common.type.id.Uuid
import com.thebrownfoxx.neon.server.route.WebSocketMessage
import io.ktor.util.reflect.TypeInfo
import io.ktor.util.reflect.typeInfo
import kotlinx.coroutines.flow.Flow

interface WebSocketSession {
    val id: WebSocketSessionId
    val incomingMessages: Flow<SerializedWebSocketMessage>
    suspend fun send(message: Any?, typeInfo: TypeInfo)
    suspend fun close()
}

suspend inline fun <reified T : WebSocketMessage> WebSocketSession.send(message: T) {
    send(message = message, typeInfo = typeInfo<T>())
}

class WebSocketSessionId(override val uuid: Uuid = Uuid()) : Id