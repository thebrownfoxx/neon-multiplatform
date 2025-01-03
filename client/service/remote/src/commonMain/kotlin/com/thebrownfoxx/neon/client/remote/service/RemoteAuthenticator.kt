package com.thebrownfoxx.neon.client.remote.service

import com.thebrownfoxx.neon.client.remote.service.extension.bodyOrNull
import com.thebrownfoxx.neon.client.remote.service.extension.enumValueOfOrNull
import com.thebrownfoxx.neon.client.service.Authenticator
import com.thebrownfoxx.neon.client.service.Authenticator.LoginError
import com.thebrownfoxx.neon.client.service.Authenticator.LogoutError
import com.thebrownfoxx.neon.client.service.TokenStorage
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.server.route.Response
import com.thebrownfoxx.neon.server.route.authentication.LoginBody
import com.thebrownfoxx.neon.server.route.authentication.LoginResponse
import com.thebrownfoxx.outcome.Failure
import com.thebrownfoxx.outcome.UnitOutcome
import com.thebrownfoxx.outcome.UnitSuccess
import com.thebrownfoxx.outcome.map.getOrElse
import com.thebrownfoxx.outcome.map.onFailure
import com.thebrownfoxx.outcome.runFailing
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.resources.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.resources.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class RemoteAuthenticator(
    private val httpClient: HttpClient,
    private val tokenStorage: TokenStorage,
    externalScope: CoroutineScope,
) : Authenticator {
    private val _loggedInMember = MutableStateFlow<MemberId?>(null)
    override val loggedInMemberId = _loggedInMember.asStateFlow()

    override val loggedIn = loggedInMemberId.map { it != null }
        .stateIn(externalScope, SharingStarted.Eagerly, false)

    override suspend fun login(username: String, password: String): UnitOutcome<LoginError> {
        val response = runFailing {
            httpClient.post(Login()) {
                contentType(ContentType.Application.Json)
                setBody(LoginBody(username, password))
            }
        }.getOrElse { return Failure(LoginError.ConnectionError) }

        val body = response.bodyOrNull<Response>()

        return when (enumValueOfOrNull<LoginResponse.Status>(body?.status)) {
            LoginResponse.Status.InvalidCredentials -> Failure(
                LoginError.InvalidCredentials
            )

            LoginResponse.Status.InternalConnectionError -> Failure(
                LoginError.UnexpectedError
            )

            null -> Failure(LoginError.UnexpectedError)
            LoginResponse.Status.Successful -> {
                val successfulBody = response.body<LoginResponse.Successful>()
                _loggedInMember.value = successfulBody.memberId
                tokenStorage.set(successfulBody.token)
                    .onFailure { Failure(LoginError.ConnectionError) }
                UnitSuccess
            }
        }
    }

    override suspend fun logout(): UnitOutcome<LogoutError> {
        _loggedInMember.value = null
        tokenStorage.clear().onFailure { Failure(LogoutError.UnexpectedError) }
        return UnitSuccess
    }
}

@Resource("/login")
class Login