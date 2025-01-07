package com.thebrownfoxx.neon.client.service.default

import com.thebrownfoxx.neon.client.repository.TokenRepository
import com.thebrownfoxx.neon.client.repository.TokenRepository.Token
import com.thebrownfoxx.neon.client.service.Authenticator
import com.thebrownfoxx.neon.client.service.Authenticator.LoginError
import com.thebrownfoxx.neon.client.service.Authenticator.LogoutError
import com.thebrownfoxx.neon.client.service.default.extension.bodyOrNull
import com.thebrownfoxx.neon.client.service.default.extension.enumValueOfOrNull
import com.thebrownfoxx.neon.server.route.Response
import com.thebrownfoxx.neon.server.route.authentication.LoginBody
import com.thebrownfoxx.neon.server.route.authentication.LoginResponse
import com.thebrownfoxx.neon.server.route.authentication.LoginRoute
import com.thebrownfoxx.outcome.Failure
import com.thebrownfoxx.outcome.UnitOutcome
import com.thebrownfoxx.outcome.UnitSuccess
import com.thebrownfoxx.outcome.map.getOrElse
import com.thebrownfoxx.outcome.map.getOrNull
import com.thebrownfoxx.outcome.map.onFailure
import com.thebrownfoxx.outcome.runFailing
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.resources.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultAuthenticator(
    private val httpClient: HttpClient,
    private val tokenRepository: TokenRepository,
    externalScope: CoroutineScope,
) : Authenticator {
    override val loggedInMemberId = tokenRepository.getAsFlow()
        .mapLatest { it.getOrNull()?.memberId }
        .stateIn(externalScope, SharingStarted.Eagerly, null)

    override val loggedIn = loggedInMemberId.map { it != null }
        .stateIn(externalScope, SharingStarted.Eagerly, false)

    override suspend fun login(username: String, password: String): UnitOutcome<LoginError> {
        val response = runFailing {
            httpClient.post(LoginRoute()) {
                contentType(ContentType.Application.Json)
                setBody(LoginBody(username, password))
            }
        }.getOrElse { return Failure(LoginError.ConnectionError) }

        val body = response.bodyOrNull<Response>()
        return when (enumValueOfOrNull<LoginResponse.Status>(body?.status)) {
            LoginResponse.Status.InvalidCredentials -> Failure(LoginError.InvalidCredentials)
            LoginResponse.Status.InternalConnectionError -> Failure(LoginError.UnexpectedError)
            null -> Failure(LoginError.UnexpectedError)
            LoginResponse.Status.Successful -> {
                val (memberId, jwt) = response.body<LoginResponse.Successful>()
                tokenRepository.set(Token(jwt, memberId))
                    .onFailure { return Failure(LoginError.ConnectionError) }
                UnitSuccess
            }
        }
    }

    override suspend fun logout(): UnitOutcome<LogoutError> {
        tokenRepository.clear().onFailure { Failure(LogoutError.UnexpectedError) }
        return UnitSuccess
    }
}
