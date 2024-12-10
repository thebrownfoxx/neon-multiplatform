package com.thebrownfoxx.neon.client.websocket

import com.thebrownfoxx.neon.common.websocket.WebSocketSession
import com.thebrownfoxx.outcome.Outcome

interface WebSocketProvider {
    suspend fun getSession(): Outcome<WebSocketSession, WebSocketConnectionError>
}