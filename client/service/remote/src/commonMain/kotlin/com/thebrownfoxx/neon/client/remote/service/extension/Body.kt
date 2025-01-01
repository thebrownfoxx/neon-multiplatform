package com.thebrownfoxx.neon.client.remote.service.extension

import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse

suspend inline fun <reified T> HttpResponse.bodyOrNull(): T? =
    runCatching { body<T>() }.getOrNull()