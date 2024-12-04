package com.thebrownfoxx.neon.server.service.default

import com.thebrownfoxx.neon.common.data.AddError
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.data.transaction.transaction
import com.thebrownfoxx.neon.common.hash.Hasher
import com.thebrownfoxx.neon.common.outcome.Failure
import com.thebrownfoxx.neon.common.outcome.Outcome
import com.thebrownfoxx.neon.common.outcome.Success
import com.thebrownfoxx.neon.common.outcome.asFailure
import com.thebrownfoxx.neon.common.outcome.getOrElse
import com.thebrownfoxx.neon.common.outcome.mapError
import com.thebrownfoxx.neon.common.outcome.onFailure
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.server.model.Member
import com.thebrownfoxx.neon.server.repository.GroupMemberRepository
import com.thebrownfoxx.neon.server.repository.InviteCodeRepository
import com.thebrownfoxx.neon.server.repository.MemberRepository
import com.thebrownfoxx.neon.server.repository.PasswordRepository
import com.thebrownfoxx.neon.server.repository.RepositoryAddMemberError
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
    // TODO: Move these to a more central place
    private val usernameMaxLength = 16
    private val passwordMinLength = 8

    override fun getMember(id: MemberId): Flow<Outcome<Member, GetMemberError>> {
        return memberRepository.getAsFlow(id).mapLatest { memberOutcome ->
            memberOutcome.mapError { error ->
                when (error) {
                    GetError.NotFound -> GetMemberError.NotFound
                    GetError.ConnectionError -> GetMemberError.InternalError
                }
            }
        }
    }

    override suspend fun registerMember(
        inviteCode: String,
        username: String,
        password: String,
    ): Outcome<MemberId, RegisterMemberError> {
        val inviteCodeGroupId = inviteCodeRepository.getGroup(inviteCode).getOrElse { error ->
            return when (error) {
                GetError.NotFound -> RegisterMemberError.InvalidInviteCode
                GetError.ConnectionError -> RegisterMemberError.InternalError
            }.asFailure()
        }

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
            memberRepository.add(member).register().onFailure { error ->
                return@transaction when (error) {
                    RepositoryAddMemberError.DuplicateId ->
                        error("Cannot add member with duplicate id")

                    RepositoryAddMemberError.DuplicateUsername ->
                        RegisterMemberError.UsernameTaken

                    RepositoryAddMemberError.ConnectionError -> RegisterMemberError.InternalError
                }.asFailure()
            }

            groupMemberRepository.addMember(inviteCodeGroupId, member.id).register()
                .onFailure { error ->
                    when (error) {
                        AddError.Duplicate -> {}
                        AddError.ConnectionError ->
                            return@transaction Failure(RegisterMemberError.InternalError)
                    }
                }

            passwordRepository.setHash(member.id, hasher.hash(password)).register()
                .onFailure { return@transaction Failure(RegisterMemberError.InternalError) }

            Success(member.id)
        }
    }
}