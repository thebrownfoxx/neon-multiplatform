package com.thebrownfoxx.neon.server.service

import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.server.model.Member
import com.thebrownfoxx.outcome.Outcome
import kotlinx.coroutines.flow.Flow

interface MemberManager {
    fun getMember(id: MemberId): Flow<Outcome<Member, GetMemberError>>

    suspend fun registerMember(
        inviteCode: String,
        username: String,
        password: String,
    ): Outcome<MemberId, RegisterMemberError>

    enum class GetMemberError {
        NotFound,
        UnexpectedError,
    }

    sealed interface RegisterMemberError {
        data object InvalidInviteCode : RegisterMemberError
        data class UsernameTooLong(val maxLength: Int) : RegisterMemberError
        data object UsernameNotAlphanumeric : RegisterMemberError
        data object UsernameTaken : RegisterMemberError
        data class PasswordTooShort(val minLength: Int) : RegisterMemberError
        data object UnexpectedError: RegisterMemberError
    }
}