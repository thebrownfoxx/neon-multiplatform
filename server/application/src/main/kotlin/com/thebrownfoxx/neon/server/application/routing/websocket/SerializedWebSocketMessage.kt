package com.thebrownfoxx.neon.server.application.routing.websocket

import com.thebrownfoxx.neon.server.route.WebSocketMessage
import io.ktor.serialization.WebsocketContentConverter
import io.ktor.serialization.deserialize
import io.ktor.util.reflect.TypeInfo
import io.ktor.util.reflect.typeInfo
import io.ktor.websocket.Frame

class SerializedWebSocketMessage(
    private val converter: WebsocketContentConverter,
    private val frame: Frame,
) {
    suspend fun getLabel() = converter.deserialize<WebSocketMessage>(frame).label

    suspend fun deserialize(typeInfo: TypeInfo): Any? = converter.deserialize(
        charset = Charsets.UTF_8,
        typeInfo = typeInfo,
        content = frame,
    )
}

suspend inline fun <reified T : WebSocketMessage> SerializedWebSocketMessage.deserialize() =
    deserialize(typeInfo<T>()) as T