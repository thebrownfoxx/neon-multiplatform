package com.thebrownfoxx.neon.server.application.routing.websocket.ktor

import com.thebrownfoxx.neon.server.application.routing.websocket.WebSocketSession
import com.thebrownfoxx.neon.server.application.routing.websocket.WebSocketSessionId
import io.ktor.server.websocket.WebSocketServerSession
import io.ktor.server.websocket.converter
import io.ktor.server.websocket.sendSerialized
import io.ktor.util.reflect.TypeInfo
import io.ktor.websocket.close
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.map

class KtorWebSocketSession(
    override val id: WebSocketSessionId = WebSocketSessionId(),
    private val session: WebSocketServerSession,
) : WebSocketSession {
    override val incomingMessages = session.incoming.consumeAsFlow()
        .map { KtorSerializedWebSocketMessage(session.converter!!, it) }

    override suspend fun send(message: Any?, typeInfo: TypeInfo) {
        session.sendSerialized(data = message, typeInfo = typeInfo)
    }

    override suspend fun close() {
        session.close()
    }
}