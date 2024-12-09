package com.thebrownfoxx.neon.client.websocket

import com.thebrownfoxx.neon.common.Logger
import com.thebrownfoxx.neon.common.websocket.ktor.KtorSerializedWebSocketMessage
import com.thebrownfoxx.neon.common.websocket.ktor.KtorWebSocketSession
import com.thebrownfoxx.neon.common.websocket.ktor.toKtorTypeInfo
import com.thebrownfoxx.neon.common.websocket.model.Type
import com.thebrownfoxx.outcome.memberBlockContext
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.converter
import io.ktor.client.plugins.websocket.sendSerialized
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class KtorClientWebSocketSession(
    private val session: DefaultClientWebSocketSession,
    logger: Logger,
) : KtorWebSocketSession(session, logger) {
    private val _close = MutableSharedFlow<Unit>()
    override val close = _close.asSharedFlow()

    override val incomingMessages = session.incoming.consumeAsFlow()
        .map { KtorSerializedWebSocketMessage(session.converter!!, it) }
        .onCompletion {
            sessionScope.launch {
                _close.emit(Unit)
            }
        }
        .shareIn(scope = sessionScope, started = SharingStarted.Eagerly)

    override suspend fun send(message: Any?, type: Type) = memberBlockContext("send") {
        runFailing {
            withContext(Dispatchers.IO) {
                session.sendSerialized(message, type.toKtorTypeInfo())
            }
        }.mapError { SendError }
    }
}