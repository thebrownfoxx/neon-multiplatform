package com.thebrownfoxx.neon.client.websocket

import com.thebrownfoxx.neon.common.data.websocket.WebSocketSession
import com.thebrownfoxx.neon.common.data.websocket.awaitClose
import com.thebrownfoxx.neon.common.data.websocket.model.WebSocketMessage
import com.thebrownfoxx.neon.common.extension.ExponentialBackoff
import com.thebrownfoxx.neon.common.extension.ExponentialBackoffValues
import com.thebrownfoxx.neon.common.extension.flow.channelFlow
import com.thebrownfoxx.neon.common.extension.flow.mirror
import com.thebrownfoxx.neon.common.type.Type
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class AutoRetryWebSocketSubscriber(
    private val webSocketSessionProvider: WebSocketSessionProvider,
    private val exponentialBackoffValues: ExponentialBackoffValues =
        defaultExponentialBackoffValues,
) : WebSocketSubscriber {
    companion object {
        private val defaultExponentialBackoffValues = ExponentialBackoffValues(
            initialDelay = 5.seconds,
            maxDelay = 32.seconds,
            factor = 2.0,
        )
    }

    override fun <R> subscribeAsFlow(
        request: WebSocketMessage?,
        requestType: Type,
        handleResponse: SubscriptionHandler<R>.() -> Unit,
    ): Flow<R> {
        return channelFlow {
            webSocketSessionProvider.session.collectLatest { session ->
                val responseExponentialBackoff = ExponentialBackoff(exponentialBackoffValues)
                while (true) {
                    subscribe(
                        scope = this,
                        request = request,
                        session = session,
                        handleResponse = handleResponse,
                        requestType = requestType,
                        responseExponentialBackoff = responseExponentialBackoff,
                    )
                }
            }
        }
    }

    private suspend fun <R> FlowCollector<R>.subscribe(
        scope: CoroutineScope,
        request: WebSocketMessage?,
        session: WebSocketSession,
        handleResponse: SubscriptionHandler<R>.() -> Unit,
        requestType: Type,
        responseExponentialBackoff: ExponentialBackoff,
    ) {
        val subscriptionHandler = SubscriptionHandler.create(
            requestId = request?.requestId,
            session = session,
            externalScope = scope,
            handleResponse = handleResponse,
        )
        val mirrorJob = scope.launch { mirror(subscriptionHandler.response) }
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