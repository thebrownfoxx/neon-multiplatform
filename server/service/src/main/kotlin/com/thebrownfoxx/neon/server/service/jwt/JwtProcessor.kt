package com.thebrownfoxx.neon.server.service.jwt

import com.auth0.jwt.interfaces.Payload
import com.thebrownfoxx.neon.common.type.Jwt
import com.thebrownfoxx.neon.server.service.jwt.model.JwtClaim
import com.thebrownfoxx.neon.server.service.jwt.model.JwtClaimKey
import com.thebrownfoxx.neon.server.service.jwt.model.JwtConfig

interface JwtProcessor {
    val config: JwtConfig

    fun generateJwt(vararg claims: JwtClaim): Jwt

    fun getClaim(
        payload: Payload,
        key: JwtClaimKey,
    ): JwtClaim?
}
