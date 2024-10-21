package com.thebrownfoxx.neon.client.application.http

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.cio.CIO

actual fun baseHttpClient(block: HttpClientConfig<*>.() -> Unit) = HttpClient(CIO, block)