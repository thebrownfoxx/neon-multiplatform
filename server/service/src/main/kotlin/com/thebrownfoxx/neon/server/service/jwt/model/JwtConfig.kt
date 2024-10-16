package com.thebrownfoxx.neon.server.service.jwt.model

import kotlin.time.Duration

data class JwtConfig(
    val realm: String,
    val issuer: String,
    val audience: String,
    val validity: Duration,
    val secret: String,
)
