package com.thebrownfoxx.neon.server.service

import com.auth0.jwt.interfaces.Payload
import com.thebrownfoxx.neon.common.type.Jwt
import kotlin.time.Duration

interface JwtProcessor {
    val config: JwtConfig

    fun generateJwt(vararg claims: JwtClaim): Jwt

    fun getClaim(
        payload: Payload,
        key: JwtClaimKey,
    ): JwtClaim?
}

data class JwtConfig(
    val realm: String,
    val issuer: String,
    val audience: String,
    val validity: Duration,
    val secret: String,
)

data class JwtClaim(
    val key: JwtClaimKey,
    val value: String,
)

infix fun JwtClaimKey.claimedAs(value: String) = JwtClaim(this, value)

data class JwtClaimKey(val name: String)