package com.thebrownfoxx.neon.server.service.default

import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.hash.Hasher
import com.thebrownfoxx.neon.common.outcome.Failure
import com.thebrownfoxx.neon.common.outcome.Outcome
import com.thebrownfoxx.neon.common.outcome.Success
import com.thebrownfoxx.neon.common.outcome.asFailure
import com.thebrownfoxx.neon.common.outcome.getOrElse
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.server.repository.MemberRepository
import com.thebrownfoxx.neon.server.repository.PasswordRepository
import com.thebrownfoxx.neon.server.service.authenticator.Authenticator
import com.thebrownfoxx.neon.server.service.authenticator.model.AuthenticationError
import com.thebrownfoxx.neon.server.service.authenticator.model.LoginError

class DefaultAuthenticator(
    private val memberRepository: MemberRepository,
    private val passwordRepository: PasswordRepository,
    private val hasher: Hasher,
) : Authenticator {
    override suspend fun exists(memberId: MemberId): Outcome<Boolean, AuthenticationError> {
        return when (val memberOutcome = memberRepository.get(memberId)) {
            is Success -> return Success(true)
            is Failure -> when (memberOutcome.error) {
                GetError.NotFound -> Success(false)
                GetError.ConnectionError ->
                    Failure(AuthenticationError.ConnectionError)
            }
        }
    }

    override suspend fun login(username: String, password: String): Outcome<MemberId, LoginError> {
        val memberId = memberRepository.getId(username).getOrElse { error ->
            return when (error) {
                GetError.NotFound -> LoginError.InvalidCredentials
                GetError.ConnectionError -> LoginError.ConnectionError
            }.asFailure()
        }

        val passwordHash = passwordRepository.getHash(memberId).getOrElse { error ->
            return when (error) {
                GetError.NotFound -> LoginError.InvalidCredentials
                GetError.ConnectionError -> LoginError.ConnectionError
            }.asFailure()
        }

        with(hasher) {
            if (password doesNotMatch passwordHash) return Failure(LoginError.InvalidCredentials)
        }

        return Success(memberId)
    }
}