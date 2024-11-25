package com.thebrownfoxx.neon.client.service.default

import com.thebrownfoxx.neon.client.service.jwt.TokenStorage
import com.thebrownfoxx.neon.client.websocket.KtorClientWebSocketSession
import com.thebrownfoxx.neon.client.websocket.model.ConnectWebSocketError
import com.thebrownfoxx.neon.common.type.Failure
import com.thebrownfoxx.neon.common.type.Outcome
import com.thebrownfoxx.neon.common.type.asFailure
import com.thebrownfoxx.neon.common.type.getOrElse
import com.thebrownfoxx.neon.common.type.onFailure
import com.thebrownfoxx.neon.common.type.runFailing
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSocketException
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.bearerAuth

suspend fun HttpClient.connectWebSocket(
    tokenStorage: TokenStorage,
): Outcome<KtorClientWebSocketSession, ConnectWebSocketError> {
    val token = tokenStorage.get().getOrElse { return Failure(ConnectWebSocketError.Unauthorized) }

    runFailing {
        webSocket(
            path = "/connect",
            request = { bearerAuth(token.value) },
        ) {
            val session = KtorClientWebSocketSession(this)
            session.incomingMessages.collect {}
        }
    }.onFailure {
        return when (it) {
            is WebSocketException -> ConnectWebSocketError.Unauthorized
            else -> ConnectWebSocketError.ConnectionError
        }.asFailure()
    }

    return Failure(ConnectWebSocketError.UnknownError)
}