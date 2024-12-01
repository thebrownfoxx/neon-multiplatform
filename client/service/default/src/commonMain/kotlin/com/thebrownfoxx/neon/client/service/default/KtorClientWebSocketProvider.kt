package com.thebrownfoxx.neon.client.service.default

import com.thebrownfoxx.neon.client.service.jwt.TokenStorage
import com.thebrownfoxx.neon.client.websocket.KtorClientWebSocketSession
import com.thebrownfoxx.neon.client.websocket.WebSocketProvider
import com.thebrownfoxx.neon.client.websocket.model.ConnectWebSocketError
import com.thebrownfoxx.neon.common.type.Failure
import com.thebrownfoxx.neon.common.type.Outcome
import com.thebrownfoxx.neon.common.type.Success
import com.thebrownfoxx.neon.common.type.getOrElse
import com.thebrownfoxx.neon.common.type.mapError
import com.thebrownfoxx.neon.common.type.runFailing
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSocketException
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.bearerAuth

class KtorClientWebSocketProvider(
    private val httpClient: HttpClient,
    private val tokenStorage: TokenStorage,
) : WebSocketProvider {
    private var session: Outcome<KtorClientWebSocketSession, ConnectWebSocketError> =
        Failure(ConnectWebSocketError.Unauthorized)

    override suspend fun getSession(): Outcome<KtorClientWebSocketSession, ConnectWebSocketError> {
        val session = session

        if (session is Success) return session

        val token = tokenStorage.get()
            .getOrElse { return Failure(ConnectWebSocketError.Unauthorized) }

        return runFailing {
            val actualSession = httpClient.webSocketSession(
                path = "/connect",
            ) {
                bearerAuth(token.value)
            }
            KtorClientWebSocketSession(actualSession)
        }.mapError {
            when (it) {
                is WebSocketException -> ConnectWebSocketError.Unauthorized
                else -> ConnectWebSocketError.ConnectionError
            }
        }.also { this.session = it }
    }
}