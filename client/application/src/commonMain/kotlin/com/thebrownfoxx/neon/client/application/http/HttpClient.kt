package com.thebrownfoxx.neon.client.application.http

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.resources.Resources
import io.ktor.http.URLProtocol
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

expect fun baseHttpClient(block: HttpClientConfig<*>.() -> Unit = {}): HttpClient

fun HttpClient() = baseHttpClient {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
    install(Resources)

    defaultRequest {
        host = "127.0.0.1"
        port = 8080
        url { protocol = URLProtocol.HTTP }
    }
}