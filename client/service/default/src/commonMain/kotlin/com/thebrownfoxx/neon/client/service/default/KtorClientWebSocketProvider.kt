package com.thebrownfoxx.neon.client.service.default

import com.thebrownfoxx.neon.client.service.Authenticator
import com.thebrownfoxx.neon.client.service.TokenStorage
import com.thebrownfoxx.neon.client.websocket.KtorClientWebSocketSession
import com.thebrownfoxx.neon.client.websocket.WebSocketConnectionError
import com.thebrownfoxx.neon.client.websocket.WebSocketProvider
import com.thebrownfoxx.outcome.Failure
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.Success
import com.thebrownfoxx.outcome.map.getOrElse
import com.thebrownfoxx.outcome.map.mapError
import com.thebrownfoxx.outcome.map.onSuccess
import com.thebrownfoxx.outcome.runFailing
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSocketException
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.bearerAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

class KtorClientWebSocketProvider(
    private val httpClient: HttpClient,
    private val tokenStorage: TokenStorage,
    private val authenticator: Authenticator,
) : WebSocketProvider {
    private val coroutineScope = CoroutineScope(Dispatchers.IO) + SupervisorJob()

    private var session: Outcome<KtorClientWebSocketSession, WebSocketConnectionError> =
        Failure(WebSocketConnectionError.Unauthorized)

    override suspend fun getSession(): Outcome<KtorClientWebSocketSession, WebSocketConnectionError> {
        val session = session

        if (session is Success) return session

        val token = tokenStorage.get()
            .getOrElse { return Failure(WebSocketConnectionError.Unauthorized) }

        return runFailing {
            val actualSession = httpClient.webSocketSession(
                host = "127.0.0.1",
                port = 8080,
                path = "/connect",
            ) {
                bearerAuth(token.value)
            }
            KtorClientWebSocketSession.start(actualSession)
        }.mapError { error ->
            when (error) {
                is WebSocketException -> WebSocketConnectionError.Unauthorized
                else -> WebSocketConnectionError.ConnectionError
            }
        }.also {
            this@KtorClientWebSocketProvider.session = it
            disconnectOnLogout()
        }
    }

    private fun disconnectOnLogout() {
        coroutineScope.launch {
            authenticator.loggedIn.collect { loggedIn ->
                if (!loggedIn) disconnect()
            }
        }
    }

    private suspend fun disconnect() {
        session.onSuccess { it.close() }
        session = Failure(WebSocketConnectionError.Unauthorized)
    }
}