package com.thebrownfoxx.neon.client.websocket

import com.thebrownfoxx.neon.common.websocket.ktor.KtorSerializedWebSocketMessage
import com.thebrownfoxx.neon.common.websocket.ktor.KtorWebSocketSession
import com.thebrownfoxx.neon.common.websocket.ktor.toKtorTypeInfo
import com.thebrownfoxx.neon.common.websocket.model.Type
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.converter
import io.ktor.client.plugins.websocket.sendSerialized
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.map

class KtorClientWebSocketSession(
    private val session: DefaultClientWebSocketSession,
) : KtorWebSocketSession(session) {
    override val incomingMessages = session.incoming.consumeAsFlow()
        .map { KtorSerializedWebSocketMessage(session.converter!!, it) }

    override suspend fun send(message: Any?, type: Type) {
        session.sendSerialized(data = message, typeInfo = type.toKtorTypeInfo())
    }
}