package com.thebrownfoxx.neon.server.application.routing.websocket.ktor

import com.thebrownfoxx.neon.server.application.dependency.DependencyProvider
import com.thebrownfoxx.neon.server.application.plugin.AuthenticationType
import com.thebrownfoxx.neon.server.application.plugin.authenticate
import com.thebrownfoxx.neon.server.application.routing.websocket.send
import com.thebrownfoxx.neon.server.route.websocket.WebSocketConnectionResponse
import io.ktor.server.routing.Route
import io.ktor.server.websocket.webSocket

fun Route.webSocketConnectionRoute() {
    with(DependencyProvider.dependencies) {
        authenticate(AuthenticationType.Jwt) {
            webSocket("/connect") {
                val webSocketManager = webSocketManager
                val session = KtorWebSocketSession(session = this)
                if (webSocketManager is KtorWebSocketManager) {
                    webSocketManager.addSession(session)
                }
                session.send(WebSocketConnectionResponse.ConnectionSuccessful())
                session.incomingMessages.collect {}
            }
        }
    }
}