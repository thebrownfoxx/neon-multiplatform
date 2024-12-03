package com.thebrownfoxx.neon.common.websocket.model

import com.thebrownfoxx.neon.common.outcome.Outcome
import com.thebrownfoxx.neon.common.outcome.flatMapError
import com.thebrownfoxx.neon.common.outcome.map
import com.thebrownfoxx.neon.common.outcome.runFailing

interface SerializedWebSocketMessage {
    suspend fun getLabel(): WebSocketMessageLabel
    suspend fun deserialize(type: Type): Outcome<Any?, SerializationError>
}

suspend inline fun <reified T : WebSocketMessage> SerializedWebSocketMessage.deserialize() =
    runFailing {
        deserialize(typeOf<T>()).map { it as T }
    }.flatMapError { SerializationError }

data object SerializationError