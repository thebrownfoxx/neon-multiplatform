package com.thebrownfoxx.neon.server.service.repository

import com.thebrownfoxx.neon.common.model.Failure
import com.thebrownfoxx.neon.common.model.MemberId
import com.thebrownfoxx.neon.common.model.Result
import com.thebrownfoxx.neon.common.model.Success
import com.thebrownfoxx.neon.server.repository.member.MemberRepository
import com.thebrownfoxx.neon.server.repository.member.model.RepositoryGetMemberError
import com.thebrownfoxx.neon.server.service.authenticator.Authenticator
import com.thebrownfoxx.neon.server.service.authenticator.model.AuthenticationError
import kotlinx.coroutines.flow.first

class DefaultAuthenticator(private val memberRepository: MemberRepository) : Authenticator {
    override suspend fun authenticate(memberId: MemberId): Result<Boolean, AuthenticationError> {
        return when (val memberResult = memberRepository.get(memberId).first()) {
            is Success -> return Success(true)
            is Failure -> when (memberResult.error) {
                RepositoryGetMemberError.NotFound -> Success(false)
                RepositoryGetMemberError.ConnectionError ->
                    Failure(AuthenticationError.ConnectionError)
            }
        }
    }
}