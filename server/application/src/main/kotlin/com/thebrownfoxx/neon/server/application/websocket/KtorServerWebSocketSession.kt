package com.thebrownfoxx.neon.server.application.websocket

import com.thebrownfoxx.neon.common.type.id.Id
import com.thebrownfoxx.neon.common.type.id.Uuid
import com.thebrownfoxx.neon.common.websocket.ktor.KtorSerializedWebSocketMessage
import com.thebrownfoxx.neon.common.websocket.ktor.KtorWebSocketSession
import com.thebrownfoxx.neon.common.websocket.ktor.toKtorTypeInfo
import com.thebrownfoxx.neon.common.websocket.model.Type
import io.ktor.server.websocket.WebSocketServerSession
import io.ktor.server.websocket.converter
import io.ktor.server.websocket.sendSerialized
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.receiveAsFlow

class KtorServerWebSocketSession(
    val id: WebSocketSessionId = WebSocketSessionId(),
    private val session: WebSocketServerSession,
) : KtorWebSocketSession(session) {
    private val _close = MutableSharedFlow<Unit>()
    override val close = _close.asSharedFlow()

    override val incomingMessages = session.incoming.receiveAsFlow()
        .map { KtorSerializedWebSocketMessage(converter = session.converter!!, frame = it) }
        .onCompletion { _close.emit(Unit) }

    override suspend fun send(message: Any?, type: Type) {
        session.sendSerialized(data = message, typeInfo = type.toKtorTypeInfo())
    }
}

class WebSocketSessionId(override val uuid: Uuid = Uuid()) : Id