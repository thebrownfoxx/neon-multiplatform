package com.thebrownfoxx.neon.server.application.websocket

import com.thebrownfoxx.neon.common.Logger
import com.thebrownfoxx.neon.common.data.websocket.WebSocketSession
import com.thebrownfoxx.neon.common.data.websocket.WebSocketSession.SendError
import com.thebrownfoxx.neon.common.data.websocket.ktor.KtorSerializedWebSocketMessage
import com.thebrownfoxx.neon.common.data.websocket.ktor.toKtorTypeInfo
import com.thebrownfoxx.neon.common.data.websocket.model.SerializedWebSocketMessage
import com.thebrownfoxx.neon.common.type.Type
import com.thebrownfoxx.neon.common.type.id.Id
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.common.type.id.Uuid
import com.thebrownfoxx.outcome.UnitOutcome
import com.thebrownfoxx.outcome.map.getOrElse
import com.thebrownfoxx.outcome.map.mapError
import com.thebrownfoxx.outcome.runFailing
import io.ktor.server.websocket.WebSocketServerSession
import io.ktor.server.websocket.converter
import io.ktor.server.websocket.sendSerialized
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

abstract class KtorServerWebSocketSession(
    val id: WebSocketSessionId = WebSocketSessionId(),
    val memberId: MemberId,
    private val session: WebSocketServerSession,
    private val logger: Logger,
) : WebSocketSession {
    override suspend fun send(message: Any?, type: Type): UnitOutcome<SendError> {
        return runFailing {
            withContext(Dispatchers.IO) {
                session.sendSerialized(data = message, typeInfo = type.toKtorTypeInfo())
                logger.logInfo("Sent: $message")
            }
        }.mapError { SendError }
    }

    override suspend fun close() {
        session.close()
    }
}


class MutableKtorServerWebSocketSession(
    id: WebSocketSessionId = WebSocketSessionId(),
    memberId: MemberId,
    private val session: WebSocketServerSession,
    private val logger: Logger,
) : KtorServerWebSocketSession(id, memberId, session, logger) {
    private val _closed = MutableStateFlow(false)
    override val closed = _closed.asStateFlow()

    private val _incomingMessages = MutableSharedFlow<SerializedWebSocketMessage>()
    override val incomingMessages = _incomingMessages.asSharedFlow()

    suspend fun emitFrame(frame: Frame) {
        val message = KtorSerializedWebSocketMessage(converter = session.converter!!, frame = frame)
        val serializedValue = message.serializedValue.getOrElse { "<unknown message>" }
        logger.logInfo("Received: $serializedValue")
        _incomingMessages.emit(message)
    }

    override suspend fun close() {
        _closed.value = true
        super.close()
    }
}

data class WebSocketSessionId(override val uuid: Uuid = Uuid()) : Id