package com.thebrownfoxx.neon.server.service.default

import com.thebrownfoxx.neon.common.data.AddError
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.outcome.Failure
import com.thebrownfoxx.neon.common.outcome.Outcome
import com.thebrownfoxx.neon.common.outcome.UnitOutcome
import com.thebrownfoxx.neon.common.outcome.asFailure
import com.thebrownfoxx.neon.common.outcome.fold
import com.thebrownfoxx.neon.common.outcome.getOrElse
import com.thebrownfoxx.neon.common.outcome.map
import com.thebrownfoxx.neon.common.outcome.mapError
import com.thebrownfoxx.neon.common.outcome.onFailure
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.server.model.Community
import com.thebrownfoxx.neon.server.model.Group
import com.thebrownfoxx.neon.server.repository.GroupMemberRepository
import com.thebrownfoxx.neon.server.repository.GroupRepository
import com.thebrownfoxx.neon.server.repository.InviteCodeRepository
import com.thebrownfoxx.neon.server.repository.MemberRepository
import com.thebrownfoxx.neon.server.repository.RepositorySetInviteCodeError
import com.thebrownfoxx.neon.server.service.group.GroupManager
import com.thebrownfoxx.neon.server.service.group.model.AddGroupMemberError
import com.thebrownfoxx.neon.server.service.group.model.CreateCommunityError
import com.thebrownfoxx.neon.server.service.group.model.GetGroupError
import com.thebrownfoxx.neon.server.service.group.model.SetInviteCodeError
import com.thebrownfoxx.neon.server.service.permission.PermissionChecker
import com.thebrownfoxx.neon.server.service.permission.model.IsGodError
import com.thebrownfoxx.neon.server.service.permission.model.IsGroupAdminError
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
        return groupRepository.getAsFlow(id).map { group ->
            group.mapError { error ->
                when (error) {
                    GetError.NotFound -> GetGroupError.NotFound(id)
                    GetError.ConnectionError -> GetGroupError.ConnectionError
                }
            }
        }
    }

    override suspend fun createCommunity(
        actorId: MemberId,
        name: String,
        isGod: Boolean,
    ): Outcome<GroupId, CreateCommunityError> {
        val isActorGod = permissionChecker.isGod(actorId).getOrElse { error ->
            return when (error) {
                IsGodError.ConnectionError -> CreateCommunityError.ConnectionError
            }.asFailure()
        }

        if (!isActorGod) return Failure(CreateCommunityError.Unauthorized(actorId))

        if (name.length > maxCommunityNameLength)
            return Failure(CreateCommunityError.NameTooLong(name, maxCommunityNameLength))

        val community = Community(
            name = name,
            avatarUrl = null,
            isGod = isGod,
        )

        return groupRepository.add(community).result.map(
            onSuccess = { community.id },
            onFailure = { error ->
                when (error) {
                    AddError.Duplicate -> error("Cannot add community with duplicate id")
                    AddError.ConnectionError -> CreateCommunityError.ConnectionError
                }
            },
        )
    }

    override suspend fun setInviteCode(
        actorId: MemberId,
        groupId: GroupId,
        inviteCode: String,
    ): UnitOutcome<SetInviteCodeError> {
        val isGod = permissionChecker.isGod(actorId).getOrElse { error ->
            return when (error) {
                IsGodError.ConnectionError -> SetInviteCodeError.ConnectionError
            }.asFailure()
        }

        val isGroupAdmin = permissionChecker.isGroupAdmin(groupId, actorId).getOrElse { error ->
            return when (error) {
                IsGroupAdminError.ConnectionError -> SetInviteCodeError.ConnectionError
            }.asFailure()
        }

        if (!(isGod) || isGroupAdmin) return Failure(SetInviteCodeError.Unauthorized(actorId))

        val inviteCodeExists = inviteCodeRepository.getGroup(inviteCode).fold(
            onSuccess = { true },
            onFailure = { error ->
                when (error) {
                    GetError.NotFound -> false
                    GetError.ConnectionError -> return Failure(SetInviteCodeError.ConnectionError)
                }
            }
        )

        if (inviteCodeExists) return Failure(SetInviteCodeError.DuplicateInviteCode(inviteCode))

        val group = groupRepository.get(groupId).getOrElse { error ->
            return when (error) {
                GetError.NotFound -> SetInviteCodeError.GroupNotFound(groupId)
                GetError.ConnectionError -> SetInviteCodeError.ConnectionError
            }.asFailure()
        }

        if (group !is Community) return Failure(SetInviteCodeError.GroupNotCommunity(groupId))

        return inviteCodeRepository.set(groupId, inviteCode).result.mapError { error ->
            when (error) {
                RepositorySetInviteCodeError.DuplicateInviteCode ->
                    SetInviteCodeError.DuplicateInviteCode(inviteCode)

                RepositorySetInviteCodeError.ConnectionError -> SetInviteCodeError.ConnectionError
            }
        }
    }

    override suspend fun addMember(
        actorId: MemberId,
        groupId: GroupId,
        memberId: MemberId,
        isAdmin: Boolean,
    ): UnitOutcome<AddGroupMemberError> {
        val isGod = permissionChecker.isGod(actorId).getOrElse { error ->
            return when (error) {
                IsGodError.ConnectionError -> AddGroupMemberError.ConnectionError
            }.asFailure()
        }

        val isGroupAdmin = permissionChecker.isGroupAdmin(groupId, actorId).getOrElse { error ->
            return when (error) {
                IsGroupAdminError.ConnectionError -> AddGroupMemberError.ConnectionError
            }.asFailure()
        }

        if (!(isGod) || isGroupAdmin) return Failure(AddGroupMemberError.Unauthorized(actorId))

        groupRepository.get(groupId).onFailure { error ->
            return when (error) {
                GetError.NotFound -> AddGroupMemberError.GroupNotFound(groupId)
                GetError.ConnectionError -> AddGroupMemberError.ConnectionError
            }.asFailure()
        }

        memberRepository.get(memberId).onFailure { error ->
            return when (error) {
                GetError.NotFound -> AddGroupMemberError.MemberNotFound(memberId)
                GetError.ConnectionError -> AddGroupMemberError.ConnectionError
            }.asFailure()
        }

        val groupMembers = groupMemberRepository.getMembers(groupId).getOrElse {
            return Failure(AddGroupMemberError.ConnectionError)
        }

        if (memberId in groupMembers) return Failure(AddGroupMemberError.AlreadyAMember(memberId))

        return groupMemberRepository.addMember(groupId, memberId, isAdmin).result.map(
            onSuccess = {},
            onFailure = { error ->
                when (error) {
                    AddError.Duplicate -> AddGroupMemberError.AlreadyAMember(memberId)
                    AddError.ConnectionError -> AddGroupMemberError.ConnectionError
                }
            },
        )
    }
}