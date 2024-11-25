package com.thebrownfoxx.neon.server.application.routing.websocket

import com.thebrownfoxx.neon.common.type.id.Id
import com.thebrownfoxx.neon.common.type.id.Uuid
import com.thebrownfoxx.neon.common.websocket.ktor.KtorSerializedWebSocketMessage
import com.thebrownfoxx.neon.common.websocket.ktor.KtorWebSocketSession
import com.thebrownfoxx.neon.common.websocket.ktor.toKtorTypeInfo
import com.thebrownfoxx.neon.common.websocket.model.Type
import io.ktor.server.websocket.WebSocketServerSession
import io.ktor.server.websocket.converter
import io.ktor.server.websocket.sendSerialized
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.map

class KtorServerWebSocketSession(
    val id: WebSocketSessionId = WebSocketSessionId(),
    private val session: WebSocketServerSession,
) : KtorWebSocketSession(session) {
    override val incomingMessages = session.incoming.consumeAsFlow()
        .map { KtorSerializedWebSocketMessage(session.converter!!, it) }

    override suspend fun send(message: Any?, type: Type) {
        session.sendSerialized(data = message, typeInfo = type.toKtorTypeInfo())
    }
}

class WebSocketSessionId(override val uuid: Uuid = Uuid()) : Id