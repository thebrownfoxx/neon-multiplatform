package com.thebrownfoxx.neon.client.websocket

import com.thebrownfoxx.neon.common.Logger
import com.thebrownfoxx.neon.common.extension.ExponentialBackoff
import com.thebrownfoxx.neon.common.websocket.WebSocketSession
import com.thebrownfoxx.neon.common.websocket.WebSocketSession.SendError
import com.thebrownfoxx.neon.common.websocket.awaitClose
import com.thebrownfoxx.neon.common.websocket.model.SerializedWebSocketMessage
import com.thebrownfoxx.neon.common.websocket.model.Type
import com.thebrownfoxx.neon.common.websocket.model.WebSocketMessage
import com.thebrownfoxx.neon.common.websocket.send
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.UnitOutcome
import com.thebrownfoxx.outcome.UnitSuccess
import com.thebrownfoxx.outcome.map.getOrNull
import com.thebrownfoxx.outcome.map.onFailure
import com.thebrownfoxx.outcome.map.onSuccess
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class AlwaysActiveWebSocketSession(
    private val logger: Logger,
) : WebSocketSession {
    private var session: WebSocketSession? = null
    private var collectionJob: Job? = null

    private val sendChannel = Channel<Message>(Channel.BUFFERED)

    /**
     * AlwaysActiveWebSocketSession will never close so this Flow will never emit anything.
     */
    override val closed = MutableStateFlow(false).asStateFlow()

    private val _incomingMessages = MutableSharedFlow<SerializedWebSocketMessage>()
    override val incomingMessages = _incomingMessages.asSharedFlow()

    override suspend fun send(message: Any?, type: Type): UnitOutcome<SendError> {
        sendChannel.send(Message(message, type))
        return UnitSuccess
    }

    /**
     * This will not actually close AlwaysActiveWebSocketSession but will close the current
     * underlying WebSocketSession.
     */
    override suspend fun close() {
        session?.close()
    }

    suspend fun connect(
        initializeSession: suspend () -> Outcome<WebSocketSession, WebSocketConnectionError>,
    ) {
        val exponentialBackoff = ExponentialBackoff(
            initialDelay = 1.seconds,
            maxDelay = 32.seconds,
            factor = 2.0,
        )
        coroutineScope {
            while (true) {
                initializeSession()
                    .onFailure { onConnectionFailure(it, log) { cancel() } }
                    .onSuccess { onConnectionSuccess(it, exponentialBackoff) }
                exponentialBackoff.delay()
            }
        }
    }

    suspend inline fun <reified T : WebSocketMessage> subscribe(
        request: T,
        crossinline handleResponse: ResponseHandler.() -> Unit,
    ): Nothing {
        val session = this
        val exponentialBackoff = ExponentialBackoff(
            initialDelay = 5.seconds,
            maxDelay = 32.seconds,
            factor = 2.0,
        )
        while (true) {
            var responded = false
            coroutineScope {
                val responseHandler = ResponseHandler
                    .create(this, session) { handleResponse() }
                session.send(request)
                exponentialBackoff.withTimeout {
                    responseHandler.awaitFirstReceived()
                    responded = true
                }
                if (responded) {
                    exponentialBackoff.reset()
                    session.awaitClose()
                }
            }
        }
    }

    private fun onConnectionFailure(
        error: WebSocketConnectionError,
        log: String,
        cancelReconnection: () -> Unit,
    ) {
        logger.logError("WebSocket connection failed. $log")
        when (error) {
            WebSocketConnectionError.Unauthorized -> {
                logger.logError("WebSocket reconnection canceled")
                cancelReconnection()
            }

            WebSocketConnectionError.ConnectionError ->
                logger.logError("Reconnecting WebSocket")
        }
    }

    private suspend fun onConnectionSuccess(
        session: WebSocketSession,
        exponentialBackoff: ExponentialBackoff,
    ) {
        logger.logInfo("WebSocket connected")
        collectionJob?.cancel()
        collectionJob?.join()
        this.session = session
        collectionJob = coroutineScope {
            launch { session.mirrorIncomingMessages() }
            launch { session.sendQueuedMessages() }
        }
        exponentialBackoff.reset()
        session.awaitClose()
        logger.logInfo("WebSocket finished. Reconnecting...")
    }

    private suspend fun WebSocketSession.mirrorIncomingMessages() {
        incomingMessages.collect {
            _incomingMessages.emit(it)
            val label = it.getLabel().getOrNull()?.value ?: "<unknown message>"
            logger.logInfo("Received: $label")
        }
    }

    private suspend fun WebSocketSession.sendQueuedMessages() {
        while (true) {
            val message = sendChannel.receive()
            val exponentialBackoff = ExponentialBackoff(
                initialDelay = 1.seconds,
                maxDelay = 32.seconds,
                factor = 2.0,
            )
            var successful = false
            while (!successful) {
                send(message.value, message.type).onSuccess {
                    logger.logInfo("Sent: ${message.value}")
                    successful = true
                }
                exponentialBackoff.delay()
            }
        }
    }
}

private data class Message(
    val value: Any?,
    val type: Type,
)