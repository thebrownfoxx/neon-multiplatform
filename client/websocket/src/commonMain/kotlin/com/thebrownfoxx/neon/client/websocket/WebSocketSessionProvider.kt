package com.thebrownfoxx.neon.client.websocket

import com.thebrownfoxx.neon.common.data.websocket.WebSocketSession
import kotlinx.coroutines.flow.Flow

interface WebSocketSessionProvider {
    val session: Flow<WebSocketSession>
}