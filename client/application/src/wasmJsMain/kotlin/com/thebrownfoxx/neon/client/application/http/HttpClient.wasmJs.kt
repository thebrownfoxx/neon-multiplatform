package com.thebrownfoxx.neon.client.application.http

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig

actual fun baseHttpClient(block: HttpClientConfig<*>.() -> Unit) = HttpClient(block)