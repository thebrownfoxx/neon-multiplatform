package com.thebrownfoxx.neon.common.data.websocket

import com.thebrownfoxx.neon.common.data.websocket.WebSocketSession.SendError
import com.thebrownfoxx.neon.common.data.websocket.model.SerializedWebSocketMessage
import com.thebrownfoxx.neon.common.data.websocket.model.WebSocketMessage
import com.thebrownfoxx.neon.common.data.websocket.model.WebSocketMessageLabel
import com.thebrownfoxx.neon.common.type.Type
import com.thebrownfoxx.neon.common.type.typeOf
import com.thebrownfoxx.outcome.UnitOutcome
import com.thebrownfoxx.outcome.map.getOrElse
import com.thebrownfoxx.outcome.map.getOrNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
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

fun WebSocketSession.incomingInstancesOf(type: Type): Flow<WebSocketMessage> {
    return incomingMessages.transform { serializedMessage ->
        if (serializedMessage.getLabel().getOrNull() != WebSocketMessageLabel(type.kClass))
            return@transform

        val message = serializedMessage.deserialize(type).getOrElse { return@transform }
        emit(message)
    }
}

inline fun <reified T : WebSocketMessage> WebSocketSession.incomingInstancesOf(): Flow<T> {
    return incomingInstancesOf(typeOf<T>()).map { it as T }
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