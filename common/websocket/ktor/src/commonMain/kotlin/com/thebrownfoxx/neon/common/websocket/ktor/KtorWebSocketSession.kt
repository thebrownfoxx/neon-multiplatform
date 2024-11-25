package com.thebrownfoxx.neon.common.websocket.ktor

import com.thebrownfoxx.neon.common.websocket.WebSocketSession
import io.ktor.websocket.close
import io.ktor.websocket.WebSocketSession as KtorWebSocketSession

abstract class KtorWebSocketSession(private val session: KtorWebSocketSession) : WebSocketSession {
    override suspend fun close() {
        session.close()
    }
}