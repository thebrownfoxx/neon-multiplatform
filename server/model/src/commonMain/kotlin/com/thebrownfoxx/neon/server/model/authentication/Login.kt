package com.thebrownfoxx.neon.server.model.authentication

import com.thebrownfoxx.neon.common.model.Jwt
import com.thebrownfoxx.neon.common.model.MemberId
import com.thebrownfoxx.neon.server.model.Response
import kotlinx.serialization.Serializable

@Serializable
data class LoginBody(val username: String, val password: String)

object LoginResponse {
    @Serializable
    class InvalidCredentials : Response(
        status = "INVALID_CREDENTIALS",
        description = "Username or password is incorrect",
    )

    @Serializable
    class ConnectionError : Response(
        status = "INTERNAL_CONNECTION_ERROR",
        description = "There was an error connecting to one of the components of the server",
    )

    @Serializable
    data class Successful(
        val memberId: MemberId,
        val token: Jwt,
    ) : Response(
        status = "SUCCESSFUL",
        description = "Successfully logged in",
    )
}