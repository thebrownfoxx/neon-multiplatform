package com.thebrownfoxx.neon.server.application.routing.websocket

interface WebSocketManager {
    fun getSession(id: WebSocketSessionId): WebSocketSession?
}