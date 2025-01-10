package com.thebrownfoxx.neon.client.application.http

import com.thebrownfoxx.neon.client.application.environment.ClientEnvironment
import com.thebrownfoxx.neon.client.application.environment.ClientEnvironmentKey
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

fun HttpClient(environment: ClientEnvironment) = HttpClient(CIO) {
    val json = Json { ignoreUnknownKeys = true }

    install(ContentNegotiation) {
        json(json)
    }

    install(Resources)

    install(WebSockets) {
        contentConverter = KotlinxWebsocketSerializationConverter(json)
    }

    defaultRequest {
        host = environment[ClientEnvironmentKey.Host]
        port = environment[ClientEnvironmentKey.Port].toInt()
        url { protocol = URLProtocol.HTTP }
    }
}