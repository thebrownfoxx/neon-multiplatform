package com.thebrownfoxx.neon.server.service.default

import com.thebrownfoxx.neon.common.hash.Hasher
import com.thebrownfoxx.neon.common.type.Failure
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.common.type.Outcome
import com.thebrownfoxx.neon.common.type.Success
import com.thebrownfoxx.neon.common.type.getOrElse
import com.thebrownfoxx.neon.server.repository.member.MemberRepository
import com.thebrownfoxx.neon.server.repository.member.model.RepositoryGetMemberError
import com.thebrownfoxx.neon.server.repository.member.model.RepositoryGetMemberIdError
import com.thebrownfoxx.neon.server.repository.password.PasswordRepository
import com.thebrownfoxx.neon.server.repository.password.model.RepositoryGetPasswordHashError
import com.thebrownfoxx.neon.server.service.authenticator.Authenticator
import com.thebrownfoxx.neon.server.service.authenticator.model.AuthenticationError
import com.thebrownfoxx.neon.server.service.authenticator.model.LoginError
import kotlinx.coroutines.flow.first

class DefaultAuthenticator(
    private val memberRepository: MemberRepository,
    private val passwordRepository: PasswordRepository,
    private val hasher: Hasher,
) : Authenticator {
    override suspend fun exists(memberId: MemberId): Outcome<Boolean, AuthenticationError> {
        return when (val memberOutcome = memberRepository.get(memberId).first()) {
            is Success -> return Success(true)
            is Failure -> when (memberOutcome.error) {
                RepositoryGetMemberError.NotFound -> Success(false)
                RepositoryGetMemberError.ConnectionError ->
                    Failure(AuthenticationError.ConnectionError)
            }
        }
    }

    override suspend fun login(username: String, password: String): Outcome<MemberId, LoginError> {
        val memberId = memberRepository.getId(username).first().getOrElse {
            val error = when (it) {
                RepositoryGetMemberIdError.NotFound -> LoginError.InvalidCredentials
                RepositoryGetMemberIdError.ConnectionError -> LoginError.ConnectionError
            }
            return Failure(error)
        }

        val passwordHash = passwordRepository.getHash(memberId).getOrElse {
            val error = when (it) {
                RepositoryGetPasswordHashError.NotFound -> LoginError.InvalidCredentials
                RepositoryGetPasswordHashError.ConnectionError -> LoginError.ConnectionError
            }
            return Failure(error)
        }

        with(hasher) {
            if (password doesNotMatch passwordHash) return Failure(LoginError.InvalidCredentials)
        }

        return Success(memberId)
    }
}