package com.thebrownfoxx.neon.server.service.default

import com.thebrownfoxx.neon.common.data.AddError
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.type.Failure
import com.thebrownfoxx.neon.common.type.Outcome
import com.thebrownfoxx.neon.common.type.UnitOutcome
import com.thebrownfoxx.neon.common.type.asFailure
import com.thebrownfoxx.neon.common.type.fold
import com.thebrownfoxx.neon.common.type.getOrElse
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.common.type.map
import com.thebrownfoxx.neon.common.type.mapError
import com.thebrownfoxx.neon.common.type.onFailure
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
import kotlinx.coroutines.flow.first
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
        return groupRepository.get(id).map { group ->
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
        val authorizationError = authorizeCreateCommunity(actorId)
        if (authorizationError != null) return Failure(authorizationError)

        if (name.length > maxCommunityNameLength)
            return Failure(CreateCommunityError.NameTooLong(name, maxCommunityNameLength))

        val community = Community(
            name = name,
            avatarUrl = null,
            isGod = isGod,
        )

        return groupRepository.add(community).map(
            onSuccess = { community.id },
            onFailure = { error ->
                when (error) {
                    AddError.Duplicate -> error("Cannot add community with duplicate id")
                    AddError.ConnectionError -> CreateCommunityError.ConnectionError
                }
            },
        )
    }

    private suspend fun authorizeCreateCommunity(actorId: MemberId): CreateCommunityError? {
        val isGod = permissionChecker.isGod(actorId).getOrElse { error ->
            return when (error) {
                IsGodError.ConnectionError -> CreateCommunityError.ConnectionError
            }
        }

        if (!isGod) return CreateCommunityError.Unauthorized(actorId)

        return null
    }

    override suspend fun setInviteCode(
        actorId: MemberId,
        groupId: GroupId,
        inviteCode: String,
    ): UnitOutcome<SetInviteCodeError> {
        val authorizationError = authorizeSetInviteCode(actorId, groupId)
        if (authorizationError != null) return Failure(authorizationError)

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

        val group = groupRepository.get(groupId).first().getOrElse { error ->
            return when (error) {
                GetError.NotFound -> SetInviteCodeError.GroupNotFound(groupId)
                GetError.ConnectionError -> SetInviteCodeError.ConnectionError
            }.asFailure()
        }

        if (group !is Community) return Failure(SetInviteCodeError.GroupNotCommunity(groupId))

        return inviteCodeRepository.set(groupId, inviteCode).mapError { error ->
            when (error) {
                RepositorySetInviteCodeError.DuplicateInviteCode ->
                    SetInviteCodeError.DuplicateInviteCode(inviteCode)

                RepositorySetInviteCodeError.ConnectionError -> SetInviteCodeError.ConnectionError
            }
        }
    }

    private suspend fun authorizeSetInviteCode(
        actorId: MemberId,
        groupId: GroupId,
    ): SetInviteCodeError? {
        val isGod = permissionChecker.isGod(actorId).getOrElse { error ->
            return when (error) {
                IsGodError.ConnectionError -> SetInviteCodeError.ConnectionError
            }
        }

        val isGroupAdmin = permissionChecker.isGroupAdmin(groupId, actorId).getOrElse { error ->
            return when (error) {
                IsGroupAdminError.ConnectionError -> SetInviteCodeError.ConnectionError
            }
        }

        if (!(isGod) || isGroupAdmin) return SetInviteCodeError.Unauthorized(actorId)

        return null
    }

    override suspend fun addMember(
        actorId: MemberId,
        groupId: GroupId,
        memberId: MemberId,
        isAdmin: Boolean,
    ): UnitOutcome<AddGroupMemberError> {
        val authorizationError = authorizeAddMember(actorId, groupId)

        if (authorizationError != null) return Failure(authorizationError)

        groupRepository.get(groupId).first().onFailure { error ->
            return when (error) {
                GetError.NotFound -> AddGroupMemberError.GroupNotFound(groupId)
                GetError.ConnectionError -> AddGroupMemberError.ConnectionError
            }.asFailure()
        }

        memberRepository.get(memberId).first().onFailure { error ->
            return when (error) {
                GetError.NotFound -> AddGroupMemberError.MemberNotFound(memberId)
                GetError.ConnectionError -> AddGroupMemberError.ConnectionError
            }.asFailure()
        }

        val groupMembers = groupMemberRepository.getMembers(groupId).first().getOrElse {
            return Failure(AddGroupMemberError.ConnectionError)
        }

        if (memberId in groupMembers) return Failure(AddGroupMemberError.AlreadyAMember(memberId))

        return groupMemberRepository.addMember(groupId, memberId, isAdmin).map(
            onSuccess = {},
            onFailure = { error ->
                when (error) {
                    AddError.Duplicate -> AddGroupMemberError.AlreadyAMember(memberId)
                    AddError.ConnectionError -> AddGroupMemberError.ConnectionError
                }
            },
        )
    }

    private suspend fun authorizeAddMember(
        actorId: MemberId,
        groupId: GroupId,
    ): AddGroupMemberError? {
        val isGod = permissionChecker.isGod(actorId).getOrElse { error ->
            return when (error) {
                IsGodError.ConnectionError -> AddGroupMemberError.ConnectionError
            }
        }

        val isGroupAdmin = permissionChecker.isGroupAdmin(groupId, actorId).getOrElse { error ->
            return when (error) {
                IsGroupAdminError.ConnectionError -> AddGroupMemberError.ConnectionError
            }
        }

        if (!(isGod) || isGroupAdmin) return AddGroupMemberError.Unauthorized(actorId)

        return null
    }
}