package com.thebrownfoxx.neon.client.websocket

import com.thebrownfoxx.neon.client.websocket.WebSocketRequester.RequestTimeout
import com.thebrownfoxx.neon.common.data.websocket.model.WebSocketMessage
import com.thebrownfoxx.neon.common.extension.supervisorScope
import com.thebrownfoxx.neon.common.extension.withTimeout
import com.thebrownfoxx.neon.common.type.Type
import com.thebrownfoxx.outcome.Failure
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.Success
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class AutoRetryWebSocketRequester(
    private val webSocketSessionProvider: WebSocketSessionProvider,
    private val requestTimeout: Duration = 5.seconds,
) : WebSocketRequester {

    override suspend fun <R> request(
        request: WebSocketMessage?,
        requestType: Type,
        handleResponse: RequestHandler<R>.() -> Unit,
    ): Outcome<R, RequestTimeout> {
        val session = webSocketSessionProvider.session.filterNotNull().first()
        var response: R? = null
        supervisorScope {
            val requestHandler = RequestHandler.create(
                webSocketSession = session,
                externalScope = this,
                handleResponse = handleResponse,
            )
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
}