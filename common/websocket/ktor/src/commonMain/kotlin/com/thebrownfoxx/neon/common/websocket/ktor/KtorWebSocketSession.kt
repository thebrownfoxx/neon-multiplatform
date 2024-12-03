package com.thebrownfoxx.neon.common.websocket.ktor

import com.thebrownfoxx.neon.common.websocket.WebSocketSession
import io.ktor.websocket.close
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.plus
import io.ktor.websocket.WebSocketSession as KtorWebSocketSession

abstract class KtorWebSocketSession(
    private val session: KtorWebSocketSession,
) : WebSocketSession() {
    override val sessionScope = CoroutineScope(Dispatchers.IO) + SupervisorJob()

    override suspend fun close() {
        session.close()
    }
}