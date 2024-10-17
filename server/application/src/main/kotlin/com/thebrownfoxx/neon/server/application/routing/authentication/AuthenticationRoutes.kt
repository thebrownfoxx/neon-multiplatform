package com.thebrownfoxx.neon.server.application.routing.authentication

import com.thebrownfoxx.neon.common.model.MemberId
import com.thebrownfoxx.neon.common.model.getOrElse
import com.thebrownfoxx.neon.server.application.dependency.DependencyProvider
import com.thebrownfoxx.neon.server.service.authenticator.model.LoginError
import com.thebrownfoxx.neon.server.service.jwt.model.Jwt
import com.thebrownfoxx.neon.server.service.jwt.model.claimedAs
import io.ktor.http.HttpStatusCode
import io.ktor.resources.Resource
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.resources.post
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import kotlinx.serialization.Serializable

fun Route.login() {
    with(DependencyProvider.dependencies) {
        post<Login> {
            val (username, password) = call.receive<LoginBody>()
            val memberId = authenticator.login(username, password).getOrElse {
                when (it) {
                    LoginError.InvalidCredentials -> {
                        call.respond(
                            HttpStatusCode.Unauthorized,
                            mapOf(
                                "status" to "INVALID_CREDENTIALS",
                                "message" to "Username or password is incorrect",
                            ),
                        )
                        return@post
                    }
                    LoginError.ConnectionError -> {
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            mapOf(
                                "status" to "INTERNAL_CONNECTION_ERROR",
                                "message" to "There was an error connecting to one of the components of the server",
                            ),
                        )
                        return@post
                    }
                }
            }

            val jwt = jwtProcessor.generateJwt(com.thebrownfoxx.neon.server.application.plugin.MemberIdClaim claimedAs memberId.value)

            call.respond(
                HttpStatusCode.OK,
                LoginResponse(
                    status = "SUCCESS",
                    message = "Successfully logged in",
                    memberId = memberId,
                    token = jwt,
                ),
            )
        }
    }
}

@Serializable
data class LoginResponse(
    val status: String,
    val message: String,
    val memberId: MemberId,
    val token: Jwt,
)

@Serializable
data class LoginBody(val username: String, val password: String)

@Resource("/login")
class Login