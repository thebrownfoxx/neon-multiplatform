package com.thebrownfoxx.neon.client.websocket

interface WebSocketConnector {
    suspend fun connect()
}