package com.thebrownfoxx.neon.server.application.routing

import com.thebrownfoxx.neon.common.websocket.send
import com.thebrownfoxx.neon.server.application.dependency.DependencyProvider
import com.thebrownfoxx.neon.server.application.plugin.AuthenticationType
import com.thebrownfoxx.neon.server.application.plugin.authenticate
import com.thebrownfoxx.neon.server.application.websocket.MutableKtorServerWebSocketSession
import com.thebrownfoxx.neon.server.application.websocket.manager.WebSocketManagers
import com.thebrownfoxx.neon.server.route.websocket.WebSocketConnectionResponse
import io.ktor.server.routing.Route
import io.ktor.server.websocket.webSocket
import kotlinx.coroutines.channels.consumeEach

fun Route.webSocketConnectionRoute() {
    with(DependencyProvider.dependencies) {
        authenticate(AuthenticationType.Jwt) {
            webSocket("/connect") {
                val session = MutableKtorServerWebSocketSession(session = this)
                webSocketManager.addSession(session)
                session.send(WebSocketConnectionResponse.ConnectionSuccessful())
                WebSocketManagers(
                    session = session,
                    groupManager = groupManager,
                )
                incoming.consumeEach { session.emitFrame(it) }
            }
        }
    }
}