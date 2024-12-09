package com.thebrownfoxx.neon.server.service.default

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.Payload
import com.thebrownfoxx.neon.common.type.Jwt
import com.thebrownfoxx.neon.server.service.JwtClaim
import com.thebrownfoxx.neon.server.service.JwtClaimKey
import com.thebrownfoxx.neon.server.service.JwtConfig
import com.thebrownfoxx.neon.server.service.JwtProcessor
import java.util.Date

class DefaultJwtProcessor(override val config: JwtConfig) : JwtProcessor {
    override fun generateJwt(vararg claims: JwtClaim): Jwt {
        val expiry = Date(System.currentTimeMillis() + config.validity.inWholeMilliseconds)
        var jwt =
            JWT
                .create()
                .withAudience(config.audience)
                .withIssuer(config.issuer)
                .withExpiresAt(expiry)

        for (claim in claims) {
            jwt = jwt.withClaim(claim.key.name, claim.value)
        }

        return Jwt(jwt.sign(Algorithm.HMAC256(config.secret)))
    }

    override fun getClaim(
        payload: Payload,
        key: JwtClaimKey,
    ): JwtClaim? =
        payload.getClaim(key.name)?.asString()?.let { value ->
            JwtClaim(key, value)
        }
}
