package com.thebrownfoxx.neon.server.application.plugin

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.thebrownfoxx.neon.common.model.MemberId
import com.thebrownfoxx.neon.common.model.getOrElse
import com.thebrownfoxx.neon.common.type.Uuid
import com.thebrownfoxx.neon.server.application.dependency.DependencyProvider
import com.thebrownfoxx.neon.server.service.jwt.model.JwtClaimKey
import com.thebrownfoxx.neon.server.service.jwt.model.JwtConfig
import io.ktor.server.application.Application
import io.ktor.server.auth.AuthenticationConfig
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.authentication
import io.ktor.server.auth.basic
import io.ktor.server.auth.jwt.JWTAuthenticationProvider
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt

fun Application.configureSecurity() {
    authentication {
        basicAuthentication()
        jwtAuthentication()
    }
}

val MemberIdClaim = JwtClaimKey("member_id")

enum class AuthenticationType {
    Basic,
    Jwt,
}

private fun AuthenticationConfig.basicAuthentication() {
    basic(AuthenticationType.Basic.name) {
        realm = "neon"
        validate { (name, password) ->
            // TODO: Don't hardcode this
            when {
                name == "admin" && password == "password" -> UserIdPrincipal(name)
                else -> null
            }
        }
    }
}

private fun AuthenticationConfig.jwtAuthentication() {
    with(DependencyProvider.dependencies) {
        jwt(AuthenticationType.Jwt.name) {
            with(jwtProcessor.config) {
                this@jwt.realm = realm

                val jwtVerifier = JWT
                    .require(Algorithm.HMAC256(secret))
                    .withAudience(audience)
                    .withIssuer(issuer)
                    .build()

                verifier(jwtVerifier)

                validate(this)
            }
        }
    }
}

private fun JWTAuthenticationProvider.Config.validate(jwtConfig: JwtConfig) {
    with(DependencyProvider.dependencies) {
        validate { credentials ->
            val memberIdClaim = jwtProcessor.getClaim(
                credentials.payload,
                MemberIdClaim,
            )

            val memberId = memberIdClaim?.value?.let { MemberId(Uuid(it)) }

            val authenticated = jwtConfig.audience in credentials.payload.audience &&
                    memberId != null &&
                    authenticator.exists(memberId).getOrElse { false }

            when {
                authenticated -> JWTPrincipal(credentials.payload)
                else -> null
            }
        }
    }
}