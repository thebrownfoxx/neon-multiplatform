package com.thebrownfoxx.neon.common.websocket

import com.thebrownfoxx.neon.common.Logger
import com.thebrownfoxx.neon.common.websocket.model.Type
import com.thebrownfoxx.outcome.UnitOutcome
import com.thebrownfoxx.outcome.map.mapError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.plus

@Deprecated("This is a temporary interop solution for interfaces that use OldWebSocketSession and will be removed soon.")
fun WebSocketSession.asOldWebSocketSession(logger: Logger) = object : OldWebSocketSession(logger) {
    override val sessionScope = CoroutineScope(Dispatchers.IO) + SupervisorJob()

    override val close: Flow<Unit> = closed.transform { closed ->
        if (closed) emit(Unit)
    }

    override val incomingMessages = this@asOldWebSocketSession.incomingMessages

    override suspend fun send(message: Any?, type: Type): UnitOutcome<SendError> {
        return this@asOldWebSocketSession.send(message, type).mapError { SendError }
    }

    override suspend fun close() {
        this@asOldWebSocketSession.close()
    }
}