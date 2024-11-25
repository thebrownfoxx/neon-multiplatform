package com.thebrownfoxx.neon.client.service.default

import com.thebrownfoxx.neon.client.service.jwt.TokenStorage
import com.thebrownfoxx.neon.client.websocket.KtorClientWebSocketSession
import com.thebrownfoxx.neon.client.websocket.WebSocketConnector
import com.thebrownfoxx.neon.client.websocket.model.ConnectWebSocketError
import com.thebrownfoxx.neon.common.type.Failure
import com.thebrownfoxx.neon.common.type.Outcome
import com.thebrownfoxx.neon.common.type.Success
import com.thebrownfoxx.neon.common.type.asFailure
import com.thebrownfoxx.neon.common.type.getOrElse
import com.thebrownfoxx.neon.common.type.onFailure
import com.thebrownfoxx.neon.common.type.runFailing
import com.thebrownfoxx.neon.common.websocket.WebSocketSession
import com.thebrownfoxx.neon.common.websocket.ktor.KtorWebSocketSession
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSocketException
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.bearerAuth

class KtorClientWebSocketConnector(
    private val httpClient: HttpClient,
    private val tokenStorage: TokenStorage,
) : WebSocketConnector {
    override suspend fun connect(): Outcome<WebSocketSession, ConnectWebSocketError> {
        val token = tokenStorage.get().getOrElse { return Failure(ConnectWebSocketError.Unauthorized) }

        var session: KtorWebSocketSession? = null

        runFailing {
            httpClient.webSocket(
                path = "/connect",
                request = { bearerAuth(token.value) },
            ) {
                session = KtorClientWebSocketSession(this).apply {
                    incomingMessages.collect {}
                }
            }
        }.onFailure {
            return when (it) {
                is WebSocketException -> ConnectWebSocketError.Unauthorized
                else -> ConnectWebSocketError.ConnectionError
            }.asFailure()
        }

        val nonNullSession = session ?: return Failure(ConnectWebSocketError.UnknownError)

        return Success(nonNullSession)
    }
}