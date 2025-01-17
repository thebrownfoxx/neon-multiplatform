package com.thebrownfoxx.neon.client.websocket

import com.thebrownfoxx.neon.common.data.websocket.WebSocketSession
import com.thebrownfoxx.neon.common.data.websocket.awaitClose
import com.thebrownfoxx.neon.common.extension.ExponentialBackoff
import com.thebrownfoxx.neon.common.extension.ExponentialBackoffValues
import com.thebrownfoxx.neon.common.extension.loop
import com.thebrownfoxx.neon.common.logError
import com.thebrownfoxx.neon.common.logInfo
import com.thebrownfoxx.neon.common.type.Jwt
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.map.onFailure
import com.thebrownfoxx.outcome.map.onSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class AutoConnectWebSocketSessionProvider(
    private val token: Flow<Jwt>,
    private val connector: WebSocketConnector,
    externalScope: CoroutineScope,
) : WebSocketSessionProvider {
    private val exponentialBackoffValues = ExponentialBackoffValues(
        initialDelay = 1.seconds,
        maxDelay = 32.seconds,
        factor = 2.0,
    )

    private val _session = MutableStateFlow<WebSocketSession?>(null)
    override val session = _session.filterNotNull()

    init {
        externalScope.launch {
            token.collect { token ->
                connect(token)
            }
        }
    }

    private suspend fun connect(token: Jwt) {
        initializeSession {
            connector.connect(token)
        }
    }

    private suspend fun initializeSession(
        initialize: suspend () -> Outcome<WebSocketSession, WebSocketConnectionError>,
    ) {
        val exponentialBackoff = ExponentialBackoff(exponentialBackoffValues)
        loop {
            initialize()
                .onFailure { onConnectionFailure(it, log, onUnauthorized = { breakLoop() }) }
                .onSuccess { onConnectionSuccess(it, exponentialBackoff) }
            exponentialBackoff.delay()
        }
    }

    private fun onConnectionFailure(
        error: WebSocketConnectionError,
        log: String,
        onUnauthorized: () -> Unit,
    ) {
        logError("WebSocket connection failed. $log")
        when (error) {
            WebSocketConnectionError.Unauthorized -> {
                logError("WebSocket reconnection canceled")
                onUnauthorized()
            }

            WebSocketConnectionError.ConnectionError ->
                logError("Reconnecting WebSocket")
        }
    }

    private suspend fun onConnectionSuccess(
        session: WebSocketSession,
        exponentialBackoff: ExponentialBackoff,
    ) {
        logInfo("WebSocket connected")
        _session.value = session
        exponentialBackoff.reset()
        session.awaitClose()
        logInfo("WebSocket finished. Reconnecting...")
    }
}