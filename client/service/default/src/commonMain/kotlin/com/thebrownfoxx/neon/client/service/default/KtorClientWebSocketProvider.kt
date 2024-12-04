package com.thebrownfoxx.neon.client.service.default

import com.thebrownfoxx.neon.client.service.authenticator.Authenticator
import com.thebrownfoxx.neon.client.service.jwt.TokenStorage
import com.thebrownfoxx.neon.client.websocket.KtorClientWebSocketSession
import com.thebrownfoxx.neon.client.websocket.WebSocketProvider
import com.thebrownfoxx.neon.client.websocket.model.ConnectWebSocketError
import com.thebrownfoxx.neon.common.outcome.Failure
import com.thebrownfoxx.neon.common.outcome.Outcome
import com.thebrownfoxx.neon.common.outcome.Success
import com.thebrownfoxx.neon.common.outcome.getOrElse
import com.thebrownfoxx.neon.common.outcome.mapError
import com.thebrownfoxx.neon.common.outcome.onSuccess
import com.thebrownfoxx.neon.common.outcome.runFailing
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

    private var session: Outcome<KtorClientWebSocketSession, ConnectWebSocketError> =
        Failure(ConnectWebSocketError.Unauthorized)

    override suspend fun getSession(): Outcome<KtorClientWebSocketSession, ConnectWebSocketError> {
        val session = session

        if (session is Success) return session

        val token = tokenStorage.get()
            .getOrElse { return Failure(ConnectWebSocketError.Unauthorized) }

        return runFailing {
            val actualSession = httpClient.webSocketSession(
                host = "127.0.0.1",
                port = 8080,
                path = "/connect",
            ) {
                bearerAuth(token.value)
            }
            KtorClientWebSocketSession(actualSession)
        }.mapError { error ->
            when (error) {
                is WebSocketException -> ConnectWebSocketError.Unauthorized
                else -> ConnectWebSocketError.ConnectionError
            }
        }.also {
            this.session = it
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
        session = Failure(ConnectWebSocketError.Unauthorized)
    }
}