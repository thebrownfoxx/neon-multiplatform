package com.thebrownfoxx.neon.common.websocket.ktor

import com.thebrownfoxx.neon.common.websocket.model.SerializedWebSocketMessage
import com.thebrownfoxx.neon.common.websocket.model.Type
import com.thebrownfoxx.neon.common.websocket.model.WebSocketMessage
import io.ktor.serialization.WebsocketContentConverter
import io.ktor.serialization.deserialize
import io.ktor.websocket.Frame

class KtorSerializedWebSocketMessage(
    private val converter: WebsocketContentConverter,
    private val frame: Frame,
) : SerializedWebSocketMessage {
    override suspend fun getLabel() = converter.deserialize<WebSocketMessage>(frame).label

    override suspend fun deserialize(type: Type): Any? = converter.deserialize(
        charset = Charsets.UTF_8,
        typeInfo = type.toKtorTypeInfo(),
        content = frame,
    )
}