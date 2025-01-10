package com.thebrownfoxx.neon.common.data.websocket.ktor

import com.thebrownfoxx.neon.common.data.websocket.model.SerializedWebSocketMessage
import com.thebrownfoxx.neon.common.data.websocket.model.SerializedWebSocketMessage.DeserializationError
import com.thebrownfoxx.neon.common.data.websocket.model.WebSocketMessage
import com.thebrownfoxx.neon.common.data.websocket.model.WebSocketMessageHeader
import com.thebrownfoxx.neon.common.type.Type
import com.thebrownfoxx.outcome.map.mapError
import com.thebrownfoxx.outcome.runFailing
import io.ktor.serialization.WebsocketContentConverter
import io.ktor.serialization.deserialize
import io.ktor.websocket.Frame

class KtorSerializedWebSocketMessage(
    private val converter: WebsocketContentConverter,
    private val frame: Frame,
) : SerializedWebSocketMessage {
    override val serializedValue = runFailing { frame.data.toString(Charsets.UTF_8) }
        .mapError { DeserializationError }

    override suspend fun getLabel() =
        runFailing { converter.deserialize<WebSocketMessageHeader>(frame).label }
            .mapError { DeserializationError }

    override suspend fun getRequestId() =
        runFailing { converter.deserialize<WebSocketMessageHeader>(frame).requestId }
            .mapError { DeserializationError }

    override suspend fun deserialize(type: Type) = runFailing {
        converter.deserialize(
            charset = Charsets.UTF_8,
            typeInfo = type.toKtorTypeInfo(),
            content = frame,
        ) as WebSocketMessage
    }.mapError { DeserializationError }
}