package com.thebrownfoxx.neon.server.application.websocket

import com.thebrownfoxx.neon.common.type.id.Id
import com.thebrownfoxx.neon.common.type.id.Uuid
import com.thebrownfoxx.neon.common.websocket.ktor.KtorSerializedWebSocketMessage
import com.thebrownfoxx.neon.common.websocket.ktor.KtorWebSocketSession
import com.thebrownfoxx.neon.common.websocket.ktor.toKtorTypeInfo
import com.thebrownfoxx.neon.common.websocket.model.SerializedWebSocketMessage
import com.thebrownfoxx.neon.common.websocket.model.Type
import io.ktor.server.websocket.WebSocketServerSession
import io.ktor.server.websocket.converter
import io.ktor.server.websocket.sendSerialized
import io.ktor.websocket.Frame
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

abstract class KtorServerWebSocketSession(
    val id: WebSocketSessionId = WebSocketSessionId(),
    private val session: WebSocketServerSession,
) : KtorWebSocketSession(session) {
    private val _close = MutableSharedFlow<Unit>()
    override val close = _close.asSharedFlow()

    override suspend fun send(message: Any?, type: Type) {
        session.sendSerialized(data = message, typeInfo = type.toKtorTypeInfo())
    }
}

class MutableKtorServerWebSocketSession(
    id: WebSocketSessionId = WebSocketSessionId(),
    private val session: WebSocketServerSession,
) : KtorServerWebSocketSession(id, session) {
    private val _close = MutableSharedFlow<Unit>(replay = 1)
    override val close = _close.asSharedFlow()

    private val _incomingMessages = MutableSharedFlow<SerializedWebSocketMessage>()
    override val incomingMessages = _incomingMessages.asSharedFlow()

    suspend fun emitFrame(frame: Frame) {
        _incomingMessages
            .emit(KtorSerializedWebSocketMessage(converter = session.converter!!, frame = frame))
    }

    override suspend fun close() {
        _close.emit(Unit)
        super.close()
    }
}

class WebSocketSessionId(override val uuid: Uuid = Uuid()) : Id