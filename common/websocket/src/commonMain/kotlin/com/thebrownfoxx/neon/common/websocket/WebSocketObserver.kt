package com.thebrownfoxx.neon.common.websocket

import com.thebrownfoxx.neon.common.websocket.model.WebSocketMessage
import com.thebrownfoxx.neon.common.websocket.model.WebSocketMessageLabel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

abstract class WebSocketObserver(protected val session: WebSocketSession) {
    protected val coroutineScope = CoroutineScope(Dispatchers.IO) + SupervisorJob()

    init {
        coroutineScope.launch {
            session.close.collect {
                close()
            }
        }
    }

    protected suspend inline fun <reified T : WebSocketMessage> send(message: T) {
        session.send(message)
    }

    protected inline fun <reified T : WebSocketMessage> subscribe(
        label: WebSocketMessageLabel,
        crossinline action: (T) -> Unit,
    ) = session.subscribe<T>(coroutineScope, label, action)

    private fun close() = coroutineScope.cancel()
}