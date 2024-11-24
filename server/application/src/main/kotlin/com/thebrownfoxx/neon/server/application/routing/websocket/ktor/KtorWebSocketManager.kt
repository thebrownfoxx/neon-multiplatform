package com.thebrownfoxx.neon.server.application.routing.websocket.ktor

import com.thebrownfoxx.neon.server.application.routing.websocket.WebSocketManager
import com.thebrownfoxx.neon.server.application.routing.websocket.WebSocketSessionId

class KtorWebSocketManager : WebSocketManager {
    private val sessions: MutableMap<WebSocketSessionId, KtorWebSocketSession> = HashMap()

    override fun getSession(id: WebSocketSessionId): KtorWebSocketSession? {
        return sessions[id]
    }

    fun addSession(session: KtorWebSocketSession) {
        sessions[session.id] = session
    }
}