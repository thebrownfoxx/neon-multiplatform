package com.thebrownfoxx.neon.server.service.default

import com.thebrownfoxx.neon.common.data.AddError
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.server.model.Community
import com.thebrownfoxx.neon.server.model.Group
import com.thebrownfoxx.neon.server.repository.GroupMemberRepository
import com.thebrownfoxx.neon.server.repository.GroupRepository
import com.thebrownfoxx.neon.server.repository.InviteCodeRepository
import com.thebrownfoxx.neon.server.repository.MemberRepository
import com.thebrownfoxx.neon.server.service.GroupManager
import com.thebrownfoxx.neon.server.service.GroupManager.AddGroupMemberError
import com.thebrownfoxx.neon.server.service.GroupManager.CreateCommunityError
import com.thebrownfoxx.neon.server.service.GroupManager.GetGroupError
import com.thebrownfoxx.neon.server.service.GroupManager.SetInviteCodeError
import com.thebrownfoxx.neon.server.service.PermissionChecker
import com.thebrownfoxx.outcome.BlockContextScope
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.Success
import com.thebrownfoxx.outcome.UnitOutcome
import com.thebrownfoxx.outcome.getOrElse
import com.thebrownfoxx.outcome.memberBlockContext
import com.thebrownfoxx.outcome.onFailure
import com.thebrownfoxx.outcome.transform
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DefaultGroupManager(
    private val permissionChecker: PermissionChecker,
    private val groupRepository: GroupRepository,
    private val memberRepository: MemberRepository,
    private val groupMemberRepository: GroupMemberRepository,
    private val inviteCodeRepository: InviteCodeRepository,
) : GroupManager {
    // TODO: Move this to a more central location
    private val maxCommunityNameLength = 16

    override fun getGroup(id: GroupId): Flow<Outcome<Group, GetGroupError>> {
        memberBlockContext("getGroup") {
            return groupRepository.getAsFlow(id).map { groupOutcome ->
                groupOutcome.mapError { it.toGetGroupError() }
            }
        }
    }

    override suspend fun createCommunity(
        actorId: MemberId,
        name: String,
        isGod: Boolean,
    ): Outcome<GroupId, CreateCommunityError> {
        memberBlockContext("createCommunity") {
            val isActorGod = permissionChecker.isGod(actorId)
                .getOrElse { return mapError(CreateCommunityError.UnexpectedError) }

            if (!isActorGod) return Failure(CreateCommunityError.Unauthorized)

            if (name.length > maxCommunityNameLength)
                return Failure(CreateCommunityError.NameTooLong(maxCommunityNameLength))

            val community = Community(
                name = name,
                avatarUrl = null,
                isGod = isGod,
            )

            return groupRepository.add(community).result.map(
                onSuccess = { community.id },
                onFailure = { CreateCommunityError.UnexpectedError },
            )
        }
    }

    override suspend fun setInviteCode(
        actorId: MemberId,
        groupId: GroupId,
        inviteCode: String,
    ): UnitOutcome<SetInviteCodeError> {
        memberBlockContext("setInviteCode") {
            val isGod = permissionChecker.isGod(actorId)
                .getOrElse { return mapError(SetInviteCodeError.UnexpectedError) }

            val isGroupAdmin = permissionChecker.isGroupAdmin(groupId, actorId)
                .getOrElse { return mapError(SetInviteCodeError.UnexpectedError) }

            if (!(isGod) || isGroupAdmin) return Failure(SetInviteCodeError.Unauthorized)

            val inviteCodeExists = inviteCodeExists(inviteCode).getOrElse { return this }
            if (inviteCodeExists) return Failure(SetInviteCodeError.DuplicateInviteCode)

            val group = groupRepository.get(groupId)
                .getOrElse { return mapError(error.getGroupErrorToSetInviteCodeError()) }

            if (group !is Community) return Failure(SetInviteCodeError.GroupNotCommunity)

            return inviteCodeRepository.set(groupId, inviteCode).result
                .mapError { it.toSetInviteCodeError() }
        }
    }

    override suspend fun addMember(
        actorId: MemberId,
        groupId: GroupId,
        memberId: MemberId,
        isAdmin: Boolean,
    ): UnitOutcome<AddGroupMemberError> {
        memberBlockContext("addMember") {
            val isGod = permissionChecker.isGod(actorId)
                .getOrElse { return mapError(AddGroupMemberError.UnexpectedError) }

            val isGroupAdmin = permissionChecker.isGroupAdmin(groupId, actorId)
                .getOrElse { return mapError(AddGroupMemberError.UnexpectedError) }

            if (!(isGod) || isGroupAdmin) return Failure(AddGroupMemberError.Unauthorized)

            groupRepository.get(groupId)
                .onFailure { return mapError(error.getGroupErrorToAddGroupMemberError()) }

            memberRepository.get(memberId)
                .onFailure { return mapError(error.getMemberErrorToAddGroupMemberError()) }

            val groupMembers = groupMemberRepository.getMembers(groupId)
                .getOrElse { return mapError(AddGroupMemberError.UnexpectedError) }

            if (memberId in groupMembers) return Failure(AddGroupMemberError.AlreadyAMember)

            return groupMemberRepository.addMember(groupId, memberId, isAdmin).result.map(
                onSuccess = {},
                onFailure = { it.toAddGroupMemberError() },
            )
        }
    }

    private suspend fun BlockContextScope.inviteCodeExists(
        inviteCode: String,
    ): Outcome<Boolean, SetInviteCodeError> {
        return inviteCodeRepository.getGroup(inviteCode).transform(
            onSuccess = { Success(true) },
            onFailure = {
                when (error) {
                    GetError.NotFound -> Success(false)
                    GetError.ConnectionError, GetError.UnexpectedError ->
                        mapError(SetInviteCodeError.UnexpectedError)
                }
            }
        )
    }

    private fun GetError.toGetGroupError() = when (this) {
        GetError.NotFound -> GetGroupError.NotFound
        GetError.ConnectionError, GetError.UnexpectedError -> GetGroupError.UnexpectedError
    }

    private fun GetError.getGroupErrorToSetInviteCodeError() = when (this) {
        GetError.NotFound -> SetInviteCodeError.GroupNotFound
        GetError.ConnectionError, GetError.UnexpectedError -> SetInviteCodeError.UnexpectedError
    }

    private fun InviteCodeRepository.SetInviteCodeError.toSetInviteCodeError() = when (this) {
        InviteCodeRepository.SetInviteCodeError.DuplicateInviteCode ->
            SetInviteCodeError.DuplicateInviteCode

        InviteCodeRepository.SetInviteCodeError.ConnectionError,
        InviteCodeRepository.SetInviteCodeError.UnexpectedError,
            -> SetInviteCodeError.UnexpectedError
    }

    private fun GetError.getGroupErrorToAddGroupMemberError() = when (this) {
        GetError.NotFound -> AddGroupMemberError.GroupNotFound
        GetError.ConnectionError, GetError.UnexpectedError -> AddGroupMemberError.UnexpectedError
    }

    private fun GetError.getMemberErrorToAddGroupMemberError() = when (this) {
        GetError.NotFound -> AddGroupMemberError.MemberNotFound
        GetError.ConnectionError, GetError.UnexpectedError -> AddGroupMemberError.UnexpectedError
    }

    private fun AddError.toAddGroupMemberError() = when (this) {
        AddError.Duplicate -> AddGroupMemberError.AlreadyAMember
        AddError.ConnectionError, AddError.UnexpectedError ->
            AddGroupMemberError.UnexpectedError
    }
}
