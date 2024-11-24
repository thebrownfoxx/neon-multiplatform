package com.thebrownfoxx.neon.server.application.routing.websocket

import com.thebrownfoxx.neon.common.type.id.Id
import com.thebrownfoxx.neon.common.type.id.Uuid
import com.thebrownfoxx.neon.server.route.WebSocketMessage
import io.ktor.server.websocket.WebSocketServerSession
import io.ktor.server.websocket.converter
import io.ktor.server.websocket.sendSerialized
import io.ktor.util.reflect.TypeInfo
import io.ktor.util.reflect.typeInfo
import io.ktor.websocket.close
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.map

class WebSocketSession(
    val id: WebSocketSessionId = WebSocketSessionId(),
    private val session: WebSocketServerSession,
) {
    val incomingMessages = session.incoming.consumeAsFlow()
        .map { SerializedWebSocketMessage(session.converter!!, it) }

    suspend fun send(message: Any?, typeInfo: TypeInfo) {
        session.sendSerialized(data = message, typeInfo = typeInfo)
    }

    suspend fun close() {
        session.close()
    }
}

class WebSocketSessionId(override val uuid: Uuid = Uuid()) : Id

suspend inline fun <reified T : WebSocketMessage> WebSocketSession.send(message: T) {
    send(message = message, typeInfo = typeInfo<T>())
}