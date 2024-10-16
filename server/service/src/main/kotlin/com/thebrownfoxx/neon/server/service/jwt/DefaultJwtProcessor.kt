package com.thebrownfoxx.neon.server.service.jwt

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.Payload
import com.thebrownfoxx.neon.server.service.jwt.model.Jwt
import com.thebrownfoxx.neon.server.service.jwt.model.JwtClaim
import com.thebrownfoxx.neon.server.service.jwt.model.JwtClaimKey
import com.thebrownfoxx.neon.server.service.jwt.model.JwtConfig
import java.util.Date

class DefaultJwtProcessor(override val config: JwtConfig) : JwtProcessor {
    override fun generateJwt(vararg claims: JwtClaim): Jwt {
        var jwt =
            JWT
                .create()
                .withAudience(config.audience)
                .withIssuer(config.issuer)
                .withExpiresAt(Date(System.currentTimeMillis() + config.validity.inWholeMilliseconds))

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
