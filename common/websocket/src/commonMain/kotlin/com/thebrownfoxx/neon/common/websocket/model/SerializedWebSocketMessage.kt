package com.thebrownfoxx.neon.common.websocket.model

import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.blockContext
import com.thebrownfoxx.outcome.map

interface SerializedWebSocketMessage {
    val serializedValue: Outcome<String, DeserializationError>
    suspend fun getLabel(): Outcome<WebSocketMessageLabel, DeserializationError>
    suspend fun getRequestId(): Outcome<RequestId?, DeserializationError>
    suspend fun deserialize(type: Type): Outcome<Any?, DeserializationError>

    data object DeserializationError
}

suspend inline fun <reified T : WebSocketMessage> SerializedWebSocketMessage.deserialize() =
    blockContext("SerializedWebSocketMessage::deserialize") {
        runFailing { deserialize(typeOf<T>()).map { it as T } }
            .flatMapError { SerializedWebSocketMessage.DeserializationError }
    }