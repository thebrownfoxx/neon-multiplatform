package com.thebrownfoxx.neon.server.service.default

import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.hash.Hasher
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.server.repository.MemberRepository
import com.thebrownfoxx.neon.server.repository.PasswordRepository
import com.thebrownfoxx.neon.server.service.Authenticator
import com.thebrownfoxx.neon.server.service.Authenticator.LoginError
import com.thebrownfoxx.neon.server.service.Authenticator.UserExistsUnexpectedError
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.Success
import com.thebrownfoxx.outcome.getOrElse
import com.thebrownfoxx.outcome.memberBlockContext
import com.thebrownfoxx.outcome.transform

class DefaultAuthenticator(
    private val memberRepository: MemberRepository,
    private val passwordRepository: PasswordRepository,
    private val hasher: Hasher,
) : Authenticator {
    override suspend fun exists(memberId: MemberId): Outcome<Boolean, UserExistsUnexpectedError> {
        memberBlockContext("exists") {
            return memberRepository.get(memberId).transform(
                onSuccess = { Success(true) },
                onFailure = {
                    when (error) {
                        GetError.NotFound -> Success(false)
                        GetError.ConnectionError, GetError.UnexpectedError ->
                            mapError(UserExistsUnexpectedError)
                    }
                }
            )
        }
    }

    override suspend fun login(username: String, password: String): Outcome<MemberId, LoginError> {
        memberBlockContext("login") {
            val memberId = memberRepository.getId(username)
                .getOrElse { return mapError(error.getMemberErrorToLoginError()) }

            val passwordHash = passwordRepository.getHash(memberId)
                .getOrElse { return mapError(error.getPasswordHashToLoginError()) }

            with(hasher) {
                if (password doesNotMatch passwordHash)
                    return Failure(LoginError.InvalidCredentials)
            }

            return Success(memberId)
        }
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