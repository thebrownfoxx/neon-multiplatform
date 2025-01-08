package com.thebrownfoxx.neon.server.route.websocket

import com.thebrownfoxx.neon.common.data.websocket.model.WebSocketMessage
import com.thebrownfoxx.neon.common.data.websocket.model.WebSocketMessageLabel
import kotlinx.serialization.Serializable

object WebSocketConnectionResponse {
    @Serializable
    class ConnectionSuccessful : WebSocketMessage(
        label = Label,
        description = "Connection successful",
    ) {
        override val requestId = null

        companion object {
            val Label = WebSocketMessageLabel("ConnectionSuccessful")
        }
    }
}