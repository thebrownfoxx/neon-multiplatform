package com.thebrownfoxx.neon.server.application.websocket

class WebSocketManager {
    private val sessions = HashMap<WebSocketSessionId, KtorServerWebSocketSession>()

    fun getSession(id: WebSocketSessionId): KtorServerWebSocketSession? {
        return sessions[id]
    }

    fun addSession(session: KtorServerWebSocketSession) {
        sessions[session.id] = session
    }

    suspend fun removeSession(id: WebSocketSessionId) {
        sessions[id]?.close()
        sessions.remove(id)
    }
}