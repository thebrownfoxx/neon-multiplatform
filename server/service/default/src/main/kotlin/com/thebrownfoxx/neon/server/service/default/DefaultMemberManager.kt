package com.thebrownfoxx.neon.server.service.default

import com.thebrownfoxx.neon.common.hash.Hasher
import com.thebrownfoxx.neon.common.type.Failure
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.common.type.Outcome
import com.thebrownfoxx.neon.common.type.Success
import com.thebrownfoxx.neon.common.type.getOrElse
import com.thebrownfoxx.neon.common.type.mapError
import com.thebrownfoxx.neon.common.type.onFailure
import com.thebrownfoxx.neon.server.model.Member
import com.thebrownfoxx.neon.server.repository.groupmember.GroupMemberRepository
import com.thebrownfoxx.neon.server.repository.groupmember.model.RepositoryAddGroupMemberError
import com.thebrownfoxx.neon.server.repository.invite.InviteCodeRepository
import com.thebrownfoxx.neon.server.repository.invite.model.RepositoryGetInviteCodeGroupError
import com.thebrownfoxx.neon.server.repository.member.MemberRepository
import com.thebrownfoxx.neon.server.repository.member.model.RepositoryAddMemberError
import com.thebrownfoxx.neon.server.repository.member.model.RepositoryGetMemberError
import com.thebrownfoxx.neon.server.repository.password.PasswordRepository
import com.thebrownfoxx.neon.server.service.member.MemberManager
import com.thebrownfoxx.neon.server.service.member.model.GetMemberError
import com.thebrownfoxx.neon.server.service.member.model.RegisterMemberError
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
    // TODO: Move these to a more central place?
    private val usernameMaxLength = 16
    private val passwordMinLength = 8

    override fun getMember(id: MemberId): Flow<Outcome<Member, GetMemberError>> {
        return memberRepository.get(id).mapLatest { memberOutcome ->
            memberOutcome.mapError { error ->
                when (error) {
                    RepositoryGetMemberError.NotFound -> GetMemberError.NotFound
                    RepositoryGetMemberError.ConnectionError -> GetMemberError.ConnectionError
                }
            }
        }
    }

    override suspend fun registerMember(
        inviteCode: String,
        username: String,
        password: String,
    ): Outcome<MemberId, RegisterMemberError> {
        val inviteCodeGroupId = inviteCodeRepository.getGroup(inviteCode).getOrElse {
            return Failure(
                when (it) {
                    RepositoryGetInviteCodeGroupError.NotFound ->
                        RegisterMemberError.InvalidInviteCode(inviteCode)

                    RepositoryGetInviteCodeGroupError.ConnectionError ->
                        RegisterMemberError.ConnectionError
                }
            )
        }

        if (username.length > usernameMaxLength)
            return Failure(RegisterMemberError.UsernameTooLong(username, usernameMaxLength))

        if (!username.all { it.isLetterOrDigit() })
            return Failure(RegisterMemberError.UsernameNotAlphanumeric(username))

        if (password.length < passwordMinLength)
            return Failure(RegisterMemberError.PasswordTooShort(passwordMinLength))

        val member = Member(
            username = username,
            avatarUrl = null,
        )

        memberRepository.add(member).onFailure {
            when (it) {
                RepositoryAddMemberError.DuplicateId ->
                    error("Cannot add member with duplicate id")

                RepositoryAddMemberError.DuplicateUsername ->
                    RegisterMemberError.UsernameTaken(username)

                RepositoryAddMemberError.ConnectionError -> RegisterMemberError.ConnectionError
            }
        }

        groupMemberRepository.addMember(inviteCodeGroupId, member.id).onFailure {
            when (it) {
                RepositoryAddGroupMemberError.DuplicateMembership -> {}
                RepositoryAddGroupMemberError.ConnectionError ->
                    RegisterMemberError.ConnectionError
            }
        }

        passwordRepository.setHash(
            memberId = member.id,
            hash = hasher.hash(password),
        )

        return Success(member.id)
    }

}