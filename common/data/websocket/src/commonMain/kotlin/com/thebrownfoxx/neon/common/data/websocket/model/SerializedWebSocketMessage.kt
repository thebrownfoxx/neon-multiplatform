package com.thebrownfoxx.neon.common.data.websocket.model

import com.thebrownfoxx.neon.common.type.Type
import com.thebrownfoxx.neon.common.type.typeOf
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.map.flatMapError
import com.thebrownfoxx.outcome.map.map
import com.thebrownfoxx.outcome.runFailing

interface SerializedWebSocketMessage {
    val serializedValue: Outcome<String, DeserializationError>
    suspend fun getLabel(): Outcome<WebSocketMessageLabel, DeserializationError>
    suspend fun getRequestId(): Outcome<RequestId?, DeserializationError>
    suspend fun deserialize(type: Type): Outcome<WebSocketMessage, DeserializationError>

    data object DeserializationError
}

suspend inline fun <reified T : WebSocketMessage> SerializedWebSocketMessage.deserialize() =
    runFailing { deserialize(typeOf<T>()).map { it as T } }
        .flatMapError { SerializedWebSocketMessage.DeserializationError }