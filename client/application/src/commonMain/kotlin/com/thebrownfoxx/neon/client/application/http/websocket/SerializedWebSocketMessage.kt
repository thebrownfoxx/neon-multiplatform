package com.thebrownfoxx.neon.client.application.http.websocket

import com.thebrownfoxx.neon.server.route.WebSocketMessage
import io.ktor.util.reflect.TypeInfo
import io.ktor.util.reflect.typeInfo

interface SerializedWebSocketMessage {
    suspend fun getLabel(): String
    suspend fun deserialize(typeInfo: TypeInfo): Any?
}

suspend inline fun <reified T : WebSocketMessage> SerializedWebSocketMessage.deserialize() =
    deserialize(typeInfo<T>()) as T