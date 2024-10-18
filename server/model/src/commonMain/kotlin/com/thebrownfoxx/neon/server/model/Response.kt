package com.thebrownfoxx.neon.server.model

import kotlinx.serialization.Serializable

@Serializable
abstract class Response(
    val status: String,
    val description: String,
)