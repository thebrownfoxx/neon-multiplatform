package com.thebrownfoxx.neon.client.websocket

import com.thebrownfoxx.neon.common.websocket.ktor.KtorSerializedWebSocketMessage
import com.thebrownfoxx.neon.common.websocket.ktor.KtorWebSocketSession
import com.thebrownfoxx.neon.common.websocket.ktor.toKtorTypeInfo
import com.thebrownfoxx.neon.common.websocket.model.Type
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.converter
import io.ktor.client.plugins.websocket.sendSerialized
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion

class KtorClientWebSocketSession(
    private val session: DefaultClientWebSocketSession,
) : KtorWebSocketSession(session) {
    private val _close = MutableSharedFlow<Unit>()
    override val close = _close.asSharedFlow()

    override val incomingMessages = session.incoming.consumeAsFlow()
        .map { KtorSerializedWebSocketMessage(session.converter!!, it) }
        .onCompletion { _close.tryEmit(Unit) }

    override suspend fun send(message: Any?, type: Type) {
        session.sendSerialized(data = message, typeInfo = type.toKtorTypeInfo())
    }
}