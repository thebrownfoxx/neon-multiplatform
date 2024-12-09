package com.thebrownfoxx.neon.server.service

import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.server.model.Group
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.UnitOutcome
import kotlinx.coroutines.flow.Flow

interface GroupManager {
    fun getGroup(id: GroupId): Flow<Outcome<Group, GetGroupError>>

    suspend fun createCommunity(
        actorId: MemberId,
        name: String,
        isGod: Boolean = false,
    ): Outcome<GroupId, CreateCommunityError>

    suspend fun setInviteCode(
        actorId: MemberId,
        groupId: GroupId,
        inviteCode: String,
    ): UnitOutcome<SetInviteCodeError>

    suspend fun addMember(
        actorId: MemberId,
        groupId: GroupId,
        memberId: MemberId,
        isAdmin: Boolean = false,
    ): UnitOutcome<AddGroupMemberError>

    enum class GetGroupError {
        NotFound,
        UnexpectedError,
    }

    sealed interface CreateCommunityError {
        data object Unauthorized : CreateCommunityError
        data class NameTooLong(val maxLength: Int) : CreateCommunityError
        data object UnexpectedError : CreateCommunityError
    }

    enum class SetInviteCodeError {
        Unauthorized,
        GroupNotFound,
        GroupNotCommunity,
        DuplicateInviteCode,
        UnexpectedError,
    }

    enum class AddGroupMemberError {
        Unauthorized,
        AlreadyAMember,
        GroupNotFound,
        MemberNotFound,
        UnexpectedError,
    }
}