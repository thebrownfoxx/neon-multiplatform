package com.thebrownfoxx.neon.server.service.jwt.model

data class JwtClaim(
    val key: JwtClaimKey,
    val value: String,
)

infix fun JwtClaimKey.claimedAs(value: String) = JwtClaim(this, value)