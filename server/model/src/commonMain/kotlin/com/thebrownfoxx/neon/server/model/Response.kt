package com.thebrownfoxx.neon.server.model

import kotlinx.serialization.Serializable

@Serializable
open class Response(
    val status: String,
    val description: String,
)