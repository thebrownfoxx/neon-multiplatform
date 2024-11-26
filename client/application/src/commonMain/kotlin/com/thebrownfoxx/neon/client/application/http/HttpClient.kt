package com.thebrownfoxx.neon.client.application.http

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.resources.Resources
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.http.URLProtocol
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun HttpClient() = HttpClient(CIO) {
    val json = Json { ignoreUnknownKeys = true }

    install(ContentNegotiation) {
        json(json)
    }

    install(Resources)

    install(WebSockets) {
        contentConverter = KotlinxWebsocketSerializationConverter(json)
    }

    defaultRequest {
        host = "127.0.0.1"
        port = 8080
        url { protocol = URLProtocol.HTTP }
    }
}