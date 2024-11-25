package com.thebrownfoxx.neon.common.websocket.model

interface SerializedWebSocketMessage {
    suspend fun getLabel(): String
    suspend fun deserialize(type: Type): Any?
}

suspend inline fun <reified T : WebSocketMessage> SerializedWebSocketMessage.deserialize() =
    deserialize(typeOf<T>()) as T