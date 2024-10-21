package com.thebrownfoxx.neon.server.application.routing.authentication

import com.thebrownfoxx.neon.common.model.getOrElse
import com.thebrownfoxx.neon.server.application.dependency.DependencyProvider
import com.thebrownfoxx.neon.server.application.plugin.MemberIdClaim
import com.thebrownfoxx.neon.server.model.authentication.LoginBody
import com.thebrownfoxx.neon.server.model.authentication.LoginResponse
import com.thebrownfoxx.neon.server.service.authenticator.model.LoginError
import com.thebrownfoxx.neon.server.service.jwt.model.claimedAs
import io.ktor.http.HttpStatusCode
import io.ktor.resources.Resource
import io.ktor.server.request.receive
import io.ktor.server.resources.post
import io.ktor.server.response.respond
import io.ktor.server.routing.Route

@Resource("/login")
private class Login

fun Route.login() {
    with(DependencyProvider.dependencies) {
        post<Login> {
            val (username, password) = call.receive<LoginBody>()
            val memberId = authenticator.login(username, password).getOrElse {
                return@post when (it) {
                    LoginError.InvalidCredentials -> call.respond(
                        HttpStatusCode.Unauthorized,
                        LoginResponse.InvalidCredentials(),
                    )

                    LoginError.ConnectionError -> call.respond(
                        HttpStatusCode.InternalServerError,
                        LoginResponse.ConnectionError(),
                    )
                }
            }

            val jwt = jwtProcessor.generateJwt(MemberIdClaim claimedAs memberId.value)

            call.respond(
                HttpStatusCode.OK,
                LoginResponse.Successful(
                    memberId = memberId,
                    token = jwt,
                ),
            )
        }
    }
}