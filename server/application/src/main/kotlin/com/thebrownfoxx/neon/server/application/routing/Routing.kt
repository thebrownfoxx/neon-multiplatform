package com.thebrownfoxx.neon.server.application.routing

import com.thebrownfoxx.neon.server.application.routing.authentication.login
import io.ktor.resources.Resource
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.resources.Resources
import io.ktor.server.resources.get
import io.ktor.server.response.respondText
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import kotlinx.serialization.json.Json

fun Application.configureRouting() {
    install(Resources)

    install(ContentNegotiation) {
        json()
    }

    install(WebSockets) {
        contentConverter = KotlinxWebsocketSerializationConverter(Json { ignoreUnknownKeys = true })
    }

    routing {
        get<HelloWorld> { call.respondText("Hello, world!") }
        login()
        webSocketConnectionRoute()
    }
}

@Resource("/hello-world")
class HelloWorld