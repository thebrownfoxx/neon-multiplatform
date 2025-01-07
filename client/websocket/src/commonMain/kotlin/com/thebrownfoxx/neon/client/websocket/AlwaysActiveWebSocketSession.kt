package com.thebrownfoxx.neon.client.websocket

import com.thebrownfoxx.neon.client.websocket.WebSocketRequester.RequestTimeout
import com.thebrownfoxx.neon.common.Logger
import com.thebrownfoxx.neon.common.extension.ExponentialBackoff
import com.thebrownfoxx.neon.common.extension.ExponentialBackoffValues
import com.thebrownfoxx.neon.common.extension.channelFlow
import com.thebrownfoxx.neon.common.extension.coroutineScope
import com.thebrownfoxx.neon.common.extension.mirror
import com.thebrownfoxx.neon.common.extension.supervisorScope
import com.thebrownfoxx.neon.common.extension.withTimeout
import com.thebrownfoxx.neon.common.websocket.WebSocketSession
import com.thebrownfoxx.neon.common.websocket.WebSocketSession.SendError
import com.thebrownfoxx.neon.common.websocket.awaitClose
import com.thebrownfoxx.neon.common.websocket.model.SerializedWebSocketMessage
import com.thebrownfoxx.neon.common.websocket.model.Type
import com.thebrownfoxx.neon.common.websocket.model.WebSocketMessage
import com.thebrownfoxx.outcome.Failure
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.Success
import com.thebrownfoxx.outcome.UnitOutcome
import com.thebrownfoxx.outcome.UnitSuccess
import com.thebrownfoxx.outcome.map.getOrNull
import com.thebrownfoxx.outcome.map.onFailure
import com.thebrownfoxx.outcome.map.onSuccess
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class AlwaysActiveWebSocketSession(
    private val logger: Logger,
) : WebSocketSession, WebSocketSubscriber, WebSocketRequester {
    private val requestTimeout = 5.seconds

    private val connectionExponentialBackoffValues = ExponentialBackoffValues(
        initialDelay = 1.seconds,
        maxDelay = 32.seconds,
        factor = 2.0,
    )

    private val responseExponentialBackoffValues = ExponentialBackoffValues(
        initialDelay = 5.seconds,
        maxDelay = 32.seconds,
        factor = 2.0,
    )

    private val session = MutableStateFlow<WebSocketSession?>(null)
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
        session.value?.close()
    }

    suspend fun connect(
        initializeSession: suspend () -> Outcome<WebSocketSession, WebSocketConnectionError>,
    ) {
        val exponentialBackoff = ExponentialBackoff(connectionExponentialBackoffValues)
        var unauthorized = false
        while (!unauthorized) {
            initializeSession()
                .onFailure { onConnectionFailure(it, log) { unauthorized = true } }
                .onSuccess { onConnectionSuccess(it, exponentialBackoff) }
            exponentialBackoff.delay()
        }
    }

    override fun <R> subscribeAsFlow(
        request: WebSocketMessage?,
        requestType: Type,
        handleResponse: SubscriptionHandler<R>.() -> Unit,
    ): Flow<R> {
        return channelFlow {
            session.collectLatest { session ->
                if (session == null) return@collectLatest
                val responseExponentialBackoff =
                    ExponentialBackoff(responseExponentialBackoffValues)
                while (true) {
                    val subscriptionHandler = SubscriptionHandler.create(
                        requestId = request?.requestId,
                        session = session,
                        externalScope = this,
                        handleResponse = handleResponse,
                    )
                    val mirrorJob = launch { mirror(subscriptionHandler.response) }
                    if (request != null) session.send(request, requestType)
                    responseExponentialBackoff.withTimeout {
                        subscriptionHandler.awaitFirst()
                        runAfterTimeout {
                            responseExponentialBackoff.reset()
                            session.awaitClose()
                        }
                    }
                    mirrorJob.cancel()
                }
            }
        }
    }

    override suspend fun <R> request(
        request: Any?,
        requestType: Type,
        handleResponse: RequestHandler<R>.() -> Unit,
    ): Outcome<R, RequestTimeout> {
        val session = session.filterNotNull().first()
        var response: R? = null
        supervisorScope {
            val requestHandler = RequestHandler
                .create(webSocketSession = session, externalScope = this) { handleResponse() }
            session.send(request, requestType)
            withTimeout(requestTimeout) {
                response = requestHandler.await()
            }
            cancel()
        }
        return when (val finalResponse = response) {
            null -> Failure(RequestTimeout)
            else -> Success(finalResponse)
        }
    }

    private fun onConnectionFailure(
        error: WebSocketConnectionError,
        log: String,
        onUnauthorized: () -> Unit,
    ) {
        logger.logError("WebSocket connection failed. $log")
        when (error) {
            WebSocketConnectionError.Unauthorized -> {
                logger.logError("WebSocket reconnection canceled")
                onUnauthorized()
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
        this.session.value = session
        collectionJob = coroutineScope {
            launch {
                launch { session.mirrorIncomingMessages() }
                launch { session.sendQueuedMessages() }
            }
        }.getOrNull()
        exponentialBackoff.reset()
        session.awaitClose()
        logger.logInfo("WebSocket finished. Reconnecting...")
    }

    private suspend fun WebSocketSession.mirrorIncomingMessages() {
        _incomingMessages.mirror(incomingMessages) {
            val label = it.serializedValue.getOrNull() ?: "<unknown message>"
            logger.logInfo("Received: $label")
            it
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