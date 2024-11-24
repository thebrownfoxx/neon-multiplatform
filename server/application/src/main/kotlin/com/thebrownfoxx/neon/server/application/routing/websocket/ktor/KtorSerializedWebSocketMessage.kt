package com.thebrownfoxx.neon.server.application.routing.websocket.ktor

import com.thebrownfoxx.neon.server.application.routing.websocket.SerializedWebSocketMessage
import com.thebrownfoxx.neon.server.route.WebSocketMessage
import io.ktor.serialization.WebsocketContentConverter
import io.ktor.serialization.deserialize
import io.ktor.util.reflect.TypeInfo
import io.ktor.websocket.Frame

class KtorSerializedWebSocketMessage(
    private val converter: WebsocketContentConverter,
    private val frame: Frame,
) : SerializedWebSocketMessage {
    override suspend fun getLabel() = converter.deserialize<WebSocketMessage>(frame).label

    override suspend fun deserialize(typeInfo: TypeInfo): Any? = converter.deserialize(
        charset = Charsets.UTF_8,
        typeInfo = typeInfo,
        content = frame,
    )
}