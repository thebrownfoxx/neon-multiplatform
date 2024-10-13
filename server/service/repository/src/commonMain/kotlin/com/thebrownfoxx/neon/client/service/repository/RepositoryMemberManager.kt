package com.thebrownfoxx.neon.client.service.repository

import com.thebrownfoxx.neon.client.repository.group.GroupRepository
import com.thebrownfoxx.neon.client.repository.group.model.GetInviteCodeGroupError
import com.thebrownfoxx.neon.client.repository.member.MemberRepository
import com.thebrownfoxx.neon.client.repository.member.model.AddMemberEntityError
import com.thebrownfoxx.neon.client.repository.member.model.GetMemberEntityError
import com.thebrownfoxx.neon.client.repository.password.PasswordRepository
import com.thebrownfoxx.neon.client.service.member.MemberManager
import com.thebrownfoxx.neon.client.service.member.model.GetMemberError
import com.thebrownfoxx.neon.client.service.member.model.RegisterMemberError
import com.thebrownfoxx.neon.common.model.Failure
import com.thebrownfoxx.neon.common.model.Member
import com.thebrownfoxx.neon.common.model.MemberId
import com.thebrownfoxx.neon.common.model.Result
import com.thebrownfoxx.neon.common.model.getOrElse
import com.thebrownfoxx.neon.common.model.map
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapLatest

@OptIn(ExperimentalCoroutinesApi::class)
class RepositoryMemberManager(
    private val memberRepository: MemberRepository,
    private val passwordRepository: PasswordRepository,
    private val groupRepository: GroupRepository,
) : MemberManager {
    // TODO: Move these to a more central place?
    private val usernameMaxLength = 16
    private val passwordMinLength = 8

    override fun getMember(id: MemberId): Flow<Result<Member, GetMemberError>> {
        return memberRepository.get(id).mapLatest {
            it.map(
                onSuccess = { it },
                onFailure = { it.toGetMemberError() },
            )
        }
    }

    private fun GetMemberEntityError.toGetMemberError() = when (this) {
        GetMemberEntityError.NotFound -> GetMemberError.NotFound
        GetMemberEntityError.ConnectionError -> GetMemberError.ConnectionError
    }

    override suspend fun registerMember(
        inviteCode: String,
        username: String,
        password: String,
    ): Result<MemberId, RegisterMemberError> {
        val inviteCodeGroupId = groupRepository.getInviteCodeGroup(inviteCode).first()
            .getOrElse { return Failure(it.toRegisterMemberError()) }

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

        val addMemberResult = memberRepository.add(member)

        if (addMemberResult is Failure) {
            when (addMemberResult.error) {
                AddMemberEntityError.DuplicateId -> TODO()
                AddMemberEntityError.ConnectionError -> TODO()
            }
        }

        TODO()
    }

    private fun GetInviteCodeGroupError.toRegisterMemberError() = when (this) {
        GetInviteCodeGroupError.NotFound -> RegisterMemberError.InvalidInviteCode
        GetInviteCodeGroupError.ConnectionError -> RegisterMemberError.ConnectionError
    }
}