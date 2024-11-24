package com.thebrownfoxx.neon.server.route.websocket

import com.thebrownfoxx.neon.server.route.WebSocketMessage
import kotlinx.serialization.Serializable

object WebSocketConnectionResponse {
    @Serializable
    class ConnectionSuccessful : WebSocketMessage(
        label = LABEL,
        description = "Connection successful",
    ) {
        companion object {
            const val LABEL = "Successful"
        }
    }
}