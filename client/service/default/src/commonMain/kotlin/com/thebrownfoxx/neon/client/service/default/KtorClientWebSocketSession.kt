package com.thebrownfoxx.neon.client.service.default

import com.thebrownfoxx.neon.common.data.websocket.WebSocketSession
import com.thebrownfoxx.neon.common.data.websocket.WebSocketSession.SendError
import com.thebrownfoxx.neon.common.data.websocket.ktor.KtorSerializedWebSocketMessage
import com.thebrownfoxx.neon.common.data.websocket.ktor.toKtorTypeInfo
import com.thebrownfoxx.neon.common.data.websocket.model.SerializedWebSocketMessage
import com.thebrownfoxx.neon.common.extension.loop
import com.thebrownfoxx.neon.common.logInfo
import com.thebrownfoxx.neon.common.type.Type
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.map.getOrElse
import com.thebrownfoxx.outcome.map.mapError
import com.thebrownfoxx.outcome.map.onFailure
import com.thebrownfoxx.outcome.map.onSuccess
import com.thebrownfoxx.outcome.runFailing
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.converter
import io.ktor.client.plugins.websocket.sendSerialized
import io.ktor.websocket.close
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class KtorClientWebSocketSession(
    private val session: DefaultClientWebSocketSession,
    externalScope: CoroutineScope,
) : WebSocketSession {
    init {
        externalScope.launch {
            loop { receiveMessages(onFailure = ::breakLoop) }
        }
    }

    private val _closed = MutableStateFlow(false)

    override val closed = _closed.asStateFlow()
    private val _incomingMessages = MutableSharedFlow<SerializedWebSocketMessage>()

    override val incomingMessages = _incomingMessages.asSharedFlow()
    override suspend fun send(message: Any?, type: Type): Outcome<Unit, SendError> {
        return runFailing {
            withContext(Dispatchers.IO) {
                session.sendSerialized(message, type.toKtorTypeInfo())
            }
        }
            .mapError { SendError }
            .onSuccess { logInfo("WS SENT: $message") }
    }

    override suspend fun close() {
        session.close()
    }

    private suspend fun receiveMessages(onFailure: () -> Unit) {
        runFailing { session.incoming.receive() }
            .onSuccess { frame ->
                val message = KtorSerializedWebSocketMessage(session.converter!!, frame)
                _incomingMessages.emit(message)
                val serializedValue = message.serializedValue.getOrElse { "<unknown message>" }
                logInfo("WS RECEIVED: $serializedValue")
            }.onFailure {
                _closed.value = true
                onFailure()
            }
    }
}