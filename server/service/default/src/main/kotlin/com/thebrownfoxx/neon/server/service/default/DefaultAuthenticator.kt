package com.thebrownfoxx.neon.server.service.default

import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.hash.Hasher
import com.thebrownfoxx.neon.common.type.Failure
import com.thebrownfoxx.neon.common.type.Outcome
import com.thebrownfoxx.neon.common.type.Success
import com.thebrownfoxx.neon.common.type.asFailure
import com.thebrownfoxx.neon.common.type.getOrElse
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.server.repository.MemberRepository
import com.thebrownfoxx.neon.server.repository.PasswordRepository
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
        /*
        TODO: IMPORTANT!! get(): Flow usually returns their old value before being updated with
         with the new value asynchronously. Although this is fine sometimes, it can lead to
         race conditions leading to unexpected behavior. For example, in this context, if the
         update about a member's deletion successfully updates the flow after .get().first() was
         executed, it would still log the user on?
         Maybe consider having separate synchronous get methods instead?
         Or maybe it isn't even worth it. research it...
         */
        return when (val memberOutcome = memberRepository.get(memberId).first()) {
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