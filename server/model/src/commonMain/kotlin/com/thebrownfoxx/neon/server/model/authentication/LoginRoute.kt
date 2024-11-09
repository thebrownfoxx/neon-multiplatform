package com.thebrownfoxx.neon.server.model.authentication

import com.thebrownfoxx.neon.common.model.Jwt
import com.thebrownfoxx.neon.common.model.MemberId
import com.thebrownfoxx.neon.server.model.Response
import io.ktor.resources.Resource
import kotlinx.serialization.Serializable

@Resource("/login")
class LoginRoute

@Serializable
data class LoginBody(val username: String, val password: String)

object LoginResponse {
    enum class Status {
        InvalidCredentials,
        InternalConnectionError,
        Successful,
    }

    @Serializable
    class InvalidCredentials : Response(
        status = Status.InvalidCredentials.name,
        description = "Username or password is incorrect",
    )

    @Serializable
    class ConnectionError : Response(
        status = Status.InternalConnectionError.name,
        description = "There was an error connecting to one of the components of the server",
    )

    @Serializable
    data class Successful(
        val memberId: MemberId,
        val token: Jwt,
    ) : Response(
        status = Status.Successful.name,
        description = "Successfully logged in",
    )
}