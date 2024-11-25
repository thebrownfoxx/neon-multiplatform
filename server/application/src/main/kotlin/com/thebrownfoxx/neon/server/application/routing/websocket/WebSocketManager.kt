package com.thebrownfoxx.neon.server.application.routing.websocket

class WebSocketManager {
    private val sessions: MutableMap<WebSocketSessionId, KtorServerWebSocketSession> = HashMap()

    fun getSession(id: WebSocketSessionId): KtorServerWebSocketSession? {
        return sessions[id]
    }

    fun addSession(session: KtorServerWebSocketSession) {
        sessions[session.id] = session
    }
}