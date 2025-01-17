package com.thebrownfoxx.neon.client.service.default

import com.thebrownfoxx.neon.client.websocket.WebSocketConnectionError
import com.thebrownfoxx.neon.client.websocket.WebSocketConnector
import com.thebrownfoxx.neon.common.Logger
import com.thebrownfoxx.neon.common.type.Jwt
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.map.mapError
import com.thebrownfoxx.outcome.runFailing
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSocketException
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.bearerAuth
import kotlinx.coroutines.CoroutineScope

class KtorClientWebSocketConnector(
    private val httpClient: HttpClient,
    private val externalScope: CoroutineScope,
    private val logger: Logger,
) : WebSocketConnector {
    override suspend fun connect(
        token: Jwt,
    ): Outcome<KtorClientWebSocketSession, WebSocketConnectionError> {
        return runFailing {
            val actualSession = httpClient.webSocketSession(
                host = "127.0.0.1",
                port = 8080,
                path = "/connect",
            ) {
                bearerAuth(token.value)
            }
            KtorClientWebSocketSession(actualSession, logger, externalScope)
        }.mapError { error ->
            when (error) {
                is WebSocketException -> WebSocketConnectionError.Unauthorized
                else -> WebSocketConnectionError.ConnectionError
            }
        }
    }
}