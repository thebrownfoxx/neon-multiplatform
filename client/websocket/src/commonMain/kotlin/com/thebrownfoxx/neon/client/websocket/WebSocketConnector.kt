package com.thebrownfoxx.neon.client.websocket

import com.thebrownfoxx.neon.common.data.websocket.WebSocketSession
import com.thebrownfoxx.neon.common.type.Jwt
import com.thebrownfoxx.outcome.Outcome

interface WebSocketConnector {
    suspend fun connect(token: Jwt): Outcome<WebSocketSession, WebSocketConnectionError>
}