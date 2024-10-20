package com.thebrownfoxx.neon.client.service.default

import com.thebrownfoxx.neon.client.service.authenticator.Authenticator
import com.thebrownfoxx.neon.client.service.authenticator.model.LoginError
import com.thebrownfoxx.neon.client.service.default.extension.bodyOrNull
import com.thebrownfoxx.neon.client.service.default.extension.enumValueOfOrNull
import com.thebrownfoxx.neon.client.service.jwt.TokenStorage
import com.thebrownfoxx.neon.client.service.jwt.model.SetTokenError
import com.thebrownfoxx.neon.common.model.Failure
import com.thebrownfoxx.neon.common.model.MemberId
import com.thebrownfoxx.neon.common.model.UnitResult
import com.thebrownfoxx.neon.common.model.onFailure
import com.thebrownfoxx.neon.common.model.unitSuccess
import com.thebrownfoxx.neon.server.model.Response
import com.thebrownfoxx.neon.server.model.authentication.LoginBody
import com.thebrownfoxx.neon.server.model.authentication.LoginResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.resources.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.resources.Resource
import kotlinx.coroutines.flow.MutableStateFlow

class DefaultAuthenticator(
    private val httpClient: HttpClient,
    private val tokenStorage: TokenStorage,
) : Authenticator {
    override val loggedInMember = MutableStateFlow<MemberId?>(null)

    override suspend fun login(username: String, password: String): UnitResult<LoginError> {
        val response = httpClient.post(Login()) {
            contentType(ContentType.Application.Json)
            setBody(LoginBody(username, password))
        }
        val body = response.bodyOrNull<Response>()

        return when (enumValueOfOrNull<LoginResponse.Status>(body?.status)) {
            LoginResponse.Status.InvalidCredentials -> Failure(LoginError.InvalidCredentials)
            LoginResponse.Status.InternalConnectionError -> Failure(LoginError.ConnectionError)
            null -> Failure(LoginError.UnknownError)
            LoginResponse.Status.Successful -> {
                val successfulBody = response.body<LoginResponse.Successful>()
                loggedInMember.value = successfulBody.memberId
                tokenStorage.set(successfulBody.token).onFailure {
                    return when (it) {
                        SetTokenError.ConnectionError -> Failure(LoginError.ConnectionError)
                    }
                }
                unitSuccess()
            }
        }
    }
}

@Resource("/login")
class Login