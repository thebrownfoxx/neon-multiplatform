package com.thebrownfoxx.neon.client.websocket

import com.thebrownfoxx.neon.client.websocket.model.ConnectWebSocketError
import com.thebrownfoxx.neon.common.websocket.WebSocketSession
import com.thebrownfoxx.outcome.Outcome

interface WebSocketProvider {
    suspend fun getSession(): Outcome<WebSocketSession, ConnectWebSocketError>
}