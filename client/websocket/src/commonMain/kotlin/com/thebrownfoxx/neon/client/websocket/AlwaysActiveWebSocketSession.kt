package com.thebrownfoxx.neon.client.websocket

import com.thebrownfoxx.neon.common.Logger
import com.thebrownfoxx.neon.common.websocket.WebSocketSession
import com.thebrownfoxx.neon.common.websocket.model.SerializedWebSocketMessage
import com.thebrownfoxx.neon.common.websocket.model.Type
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.UnitOutcome
import com.thebrownfoxx.outcome.UnitSuccess
import com.thebrownfoxx.outcome.map.onFailure
import com.thebrownfoxx.outcome.map.onSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlin.time.Duration.Companion.seconds

class AlwaysActiveWebSocketSession(
    private val logger: Logger,
) : WebSocketSession(logger) {
    private val minBackoffTime = 1.seconds
    private val maxBackoffTime = 32.seconds

    override val sessionScope: CoroutineScope = CoroutineScope(Dispatchers.IO) + SupervisorJob()
    private var session: WebSocketSession? = null
    private var collectionJob: Job? = null
    private var sendQueue = ArrayDeque<Message>()
    private var backoffTime = minBackoffTime

    /**
     * AlwaysActiveWebSocketSession will never close so this Flow will never emit anything.
     */
    override val close = flow<Unit> {  }

    private val _incomingMessages = MutableSharedFlow<SerializedWebSocketMessage>()
    override val incomingMessages = _incomingMessages.asSharedFlow()

    override suspend fun send(message: Any?, type: Type): UnitOutcome<SendError> {
        sendQueue.addLast(Message(message, type))
        return UnitSuccess
    }

    /**
     * This will not actually close AlwaysActiveWebSocketSession but will close the current
     * underlying WebSocketSession.
     */
    override suspend fun close() {
        session?.close()
    }

    fun connect(
        initializeSession: suspend () -> Outcome<WebSocketSession, WebSocketConnectionError>,
    ) {
        sessionScope.launch {
            var sessionOutcome = tryConnecting(initializeSession)
            while (true) {
                val cancelReconnection = { cancel() }
                val reconnect = suspend { sessionOutcome = tryConnecting(initializeSession) }

                sessionOutcome
                    .onFailure { onConnectionFailure(it, log, cancelReconnection, reconnect) }
                    .onSuccess { onSuccessfulConnection(it, reconnect) }
                delay(backoffTime)
            }
        }
    }

    private suspend fun onConnectionFailure(
        error: WebSocketConnectionError,
        log: String,
        cancelReconnection: () -> Unit,
        reconnect: suspend () -> Unit,
    ) {
        logger.logError("WebSocket connection failed. $log")
        when (error) {
            WebSocketConnectionError.Unauthorized -> {
                logger.logError("WebSocket reconnection canceled")
                cancelReconnection()
            }

            WebSocketConnectionError.ConnectionError -> {
                logger.logError("Reconnecting WebSocket with backoff time $backoffTime")
                reconnect()
                backoffTime = minOf(backoffTime * 2, maxBackoffTime)
            }
        }
    }

    private suspend fun onSuccessfulConnection(
        session: WebSocketSession,
        reconnect: suspend () -> Unit,
    ) {
        logger.logInfo("WebSocket connected")
        backoffTime = minBackoffTime
        session.incomingMessages.last()
        logger.logInfo("WebSocket finished. Reconnecting...")
        reconnect()
    }

    private suspend fun tryConnecting(
        initializeSession: suspend () -> Outcome<WebSocketSession, WebSocketConnectionError>,
    ): Outcome<WebSocketSession, WebSocketConnectionError> {
        return initializeSession().onSuccess { session ->
            collectionJob?.cancel()
            collectionJob?.join()
            this.session = session
            collectionJob = sessionScope.launch {
                launch { session.mirrorIncomingMessages() }
                launch { session.sendQueuedMessages() }
            }
        }
    }

    private suspend fun WebSocketSession.mirrorIncomingMessages() {
        incomingMessages.collect { _incomingMessages.emit(it) }
    }

    private suspend fun WebSocketSession.sendQueuedMessages() {
        while (sendQueue.isNotEmpty()) {
            val (message, type) = sendQueue.removeFirst()
            send(message, type)
        }
    }
}

private data class Message(
    val value: Any?,
    val type: Type,
)