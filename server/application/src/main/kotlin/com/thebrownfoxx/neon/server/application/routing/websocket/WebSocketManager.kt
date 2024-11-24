package com.thebrownfoxx.neon.server.application.routing.websocket

class WebSocketManager {
    private val sessions: MutableMap<WebSocketSessionId, WebSocketSession> = HashMap()

    fun getSession(id: WebSocketSessionId): WebSocketSession? {
        return sessions[id]
    }

    fun addSession(session: WebSocketSession) {
        sessions[session.id] = session
    }
}