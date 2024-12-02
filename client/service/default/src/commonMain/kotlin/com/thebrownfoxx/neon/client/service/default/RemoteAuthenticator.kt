package com.thebrownfoxx.neon.client.service.default

import com.thebrownfoxx.neon.client.service.authenticator.Authenticator
import com.thebrownfoxx.neon.client.service.authenticator.model.LoginError
import com.thebrownfoxx.neon.client.service.default.extension.bodyOrNull
import com.thebrownfoxx.neon.client.service.default.extension.enumValueOfOrNull
import com.thebrownfoxx.neon.client.service.jwt.TokenStorage
import com.thebrownfoxx.neon.client.service.jwt.model.SetTokenError
import com.thebrownfoxx.neon.common.outcome.Failure
import com.thebrownfoxx.neon.common.outcome.UnitOutcome
import com.thebrownfoxx.neon.common.outcome.getOrElse
import com.thebrownfoxx.neon.common.outcome.onFailure
import com.thebrownfoxx.neon.common.outcome.runFailing
import com.thebrownfoxx.neon.common.outcome.unitSuccess
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.server.route.Response
import com.thebrownfoxx.neon.server.route.authentication.LoginBody
import com.thebrownfoxx.neon.server.route.authentication.LoginResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.resources.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.resources.Resource
import kotlinx.coroutines.flow.MutableStateFlow

class RemoteAuthenticator(
    private val httpClient: HttpClient,
    private val tokenStorage: TokenStorage,
) : Authenticator {
    override val loggedInMember = MutableStateFlow<MemberId?>(null)

    override suspend fun login(username: String, password: String): UnitOutcome<LoginError> {
        val response = runFailing {
            httpClient.post(Login()) {
                contentType(ContentType.Application.Json)
                setBody(LoginBody(username, password))
            }
        }.getOrElse { return Failure(LoginError.ConnectionError) }

        val body = response.bodyOrNull<Response>()

        return when (enumValueOfOrNull<LoginResponse.Status>(body?.status)) {
            LoginResponse.Status.InvalidCredentials -> Failure(LoginError.InvalidCredentials)
            LoginResponse.Status.InternalConnectionError -> Failure(LoginError.UnknownError)
            null -> Failure(LoginError.UnknownError)
            LoginResponse.Status.Successful -> {
                val successfulBody = response.body<LoginResponse.Successful>()
                loggedInMember.value = successfulBody.memberId
                tokenStorage.set(successfulBody.token).onFailure { error ->
                    return when (error) {
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