package com.thebrownfoxx.neon.server.service.default

import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.data.transaction.transaction
import com.thebrownfoxx.neon.common.hash.Hasher
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.server.model.Member
import com.thebrownfoxx.neon.server.repository.GroupMemberRepository
import com.thebrownfoxx.neon.server.repository.InviteCodeRepository
import com.thebrownfoxx.neon.server.repository.MemberRepository
import com.thebrownfoxx.neon.server.repository.MemberRepository.AddMemberError
import com.thebrownfoxx.neon.server.repository.PasswordRepository
import com.thebrownfoxx.neon.server.service.MemberManager
import com.thebrownfoxx.neon.server.service.MemberManager.GetMemberError
import com.thebrownfoxx.neon.server.service.MemberManager.RegisterMemberError
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.Success
import com.thebrownfoxx.outcome.getOrElse
import com.thebrownfoxx.outcome.memberBlockContext
import com.thebrownfoxx.outcome.onFailure
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultMemberManager(
    private val memberRepository: MemberRepository,
    private val passwordRepository: PasswordRepository,
    private val inviteCodeRepository: InviteCodeRepository,
    private val groupMemberRepository: GroupMemberRepository,
    private val hasher: Hasher,
) : MemberManager {
    // TODO: Move these to a more central place
    private val usernameMaxLength = 16
    private val passwordMinLength = 8

    override fun getMember(id: MemberId): Flow<Outcome<Member, GetMemberError>> {
        memberBlockContext("getMember") {
            return memberRepository.getAsFlow(id).mapLatest { memberOutcome ->
                memberOutcome.mapError { it.toGetMemberError() }
            }
        }
    }

    override suspend fun registerMember(
        inviteCode: String,
        username: String,
        password: String,
    ): Outcome<MemberId, RegisterMemberError> {
        memberBlockContext("registerMember") {
            val inviteCodeGroupId = inviteCodeRepository.getGroup(inviteCode)
                .getOrElse { return mapError(error.getInviteCodeGroupToRegisterMemberError()) }

            if (username.length > usernameMaxLength)
                return Failure(RegisterMemberError.UsernameTooLong(usernameMaxLength))

            if (!username.all { it.isLetterOrDigit() })
                return Failure(RegisterMemberError.UsernameNotAlphanumeric)

            if (password.length < passwordMinLength)
                return Failure(RegisterMemberError.PasswordTooShort(passwordMinLength))

            val member = Member(
                username = username,
                avatarUrl = null,
            )

            return transaction {
                memberRepository.add(member).register()
                    .onFailure { return@transaction mapError(error.toRegisterMemberError()) }

                groupMemberRepository.addMember(inviteCodeGroupId, member.id).register()
                    .onFailure { return@transaction mapError(RegisterMemberError.UnexpectedError) }

                passwordRepository.setHash(member.id, hasher.hash(password)).register()
                    .onFailure { return@transaction mapError(RegisterMemberError.UnexpectedError) }

                Success(member.id)
            }
        }
    }

    private fun GetError.toGetMemberError() = when (this) {
        GetError.NotFound -> GetMemberError.NotFound
        GetError.ConnectionError, GetError.UnexpectedError -> GetMemberError.UnexpectedError
    }

    private fun GetError.getInviteCodeGroupToRegisterMemberError() = when (this) {
        GetError.NotFound -> RegisterMemberError.InvalidInviteCode
        GetError.ConnectionError, GetError.UnexpectedError -> RegisterMemberError.UnexpectedError
    }

    private fun AddMemberError.toRegisterMemberError() = when (this) {
        AddMemberError.DuplicateUsername -> RegisterMemberError.UsernameTaken
        AddMemberError.DuplicateId, AddMemberError.ConnectionError, AddMemberError.UnexpectedError ->
            RegisterMemberError.UnexpectedError
    }
}
