package com.thebrownfoxx.neon.common.websocket

import com.thebrownfoxx.neon.common.websocket.WebSocketSession.SendError
import com.thebrownfoxx.neon.common.websocket.model.SerializedWebSocketMessage
import com.thebrownfoxx.neon.common.websocket.model.Type
import com.thebrownfoxx.neon.common.websocket.model.WebSocketMessage
import com.thebrownfoxx.neon.common.websocket.model.WebSocketMessageLabel
import com.thebrownfoxx.neon.common.websocket.model.deserialize
import com.thebrownfoxx.neon.common.websocket.model.typeOf
import com.thebrownfoxx.outcome.UnitOutcome
import com.thebrownfoxx.outcome.map.getOrElse
import com.thebrownfoxx.outcome.map.getOrNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch

interface WebSocketSession {
    val closed: StateFlow<Boolean>
    val incomingMessages: SharedFlow<SerializedWebSocketMessage>
    suspend fun send(message: Any?, type: Type): UnitOutcome<SendError>
    suspend fun close()

    data object SendError
}

suspend inline fun <reified T : WebSocketMessage> WebSocketSession.send(
    message: T,
): UnitOutcome<SendError> {
    return send(message, typeOf<T>())
}

suspend inline fun <reified T : WebSocketMessage> WebSocketSession.incomingInstancesOf(): Flow<T> {
    return incomingMessages.transform { serializedMessage ->
        if (serializedMessage.getLabel().getOrNull() == WebSocketMessageLabel(T::class)) {
            val message = serializedMessage.deserialize<T>().getOrElse { return@transform }
            emit(message)
        }
    }
}

inline fun <reified T : WebSocketMessage> WebSocketSession.listen(
    coroutineScope: CoroutineScope,
    crossinline action: suspend (T) -> Unit,
) {
    coroutineScope.launch {
        incomingInstancesOf<T>().collect { action(it) }
    }
}

suspend fun WebSocketSession.awaitClose() {
    closed.first { it }
}