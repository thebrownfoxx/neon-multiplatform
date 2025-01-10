package com.thebrownfoxx.neon.server.service.default

import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.hash.Hasher
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.server.repository.MemberRepository
import com.thebrownfoxx.neon.server.repository.PasswordRepository
import com.thebrownfoxx.neon.server.service.Authenticator
import com.thebrownfoxx.neon.server.service.Authenticator.LoginError
import com.thebrownfoxx.neon.server.service.Authenticator.UserExistsUnexpectedError
import com.thebrownfoxx.outcome.Failure
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.Success
import com.thebrownfoxx.outcome.map.getOrElse
import com.thebrownfoxx.outcome.map.transform

class DefaultAuthenticator(
    private val memberRepository: MemberRepository,
    private val passwordRepository: PasswordRepository,
    private val hasher: Hasher,
) : Authenticator {
    override suspend fun exists(memberId: MemberId): Outcome<Boolean, UserExistsUnexpectedError> {
        return memberRepository.get(memberId).transform(
            onSuccess = { Success(true) },
            onFailure = { error ->
                when (error) {
                    GetError.NotFound -> Success(false)
                    GetError.ConnectionError, GetError.UnexpectedError ->
                        Failure(UserExistsUnexpectedError)
                }
            }
        )
    }

    override suspend fun login(username: String, password: String): Outcome<MemberId, LoginError> {
        val memberId = memberRepository.getId(username)
            .getOrElse { return Failure(it.getMemberErrorToLoginError()) }

        val passwordHash = passwordRepository.getHash(memberId)
            .getOrElse { return Failure(it.getPasswordHashToLoginError()) }

        with(hasher) {
            if (password doesNotMatch passwordHash)
                return Failure(LoginError.InvalidCredentials)
        }

        return Success(memberId)
    }

    private fun GetError.getMemberErrorToLoginError() = when (this) {
        GetError.NotFound -> LoginError.InvalidCredentials
        GetError.ConnectionError, GetError.UnexpectedError -> LoginError.UnexpectedError
    }

    private fun GetError.getPasswordHashToLoginError() = when (this) {
        GetError.NotFound -> LoginError.InvalidCredentials
        GetError.ConnectionError, GetError.UnexpectedError -> LoginError.UnexpectedError
    }
}