package com.thebrownfoxx.neon.server.service.default

import com.thebrownfoxx.neon.common.model.Failure
import com.thebrownfoxx.neon.common.model.GroupId
import com.thebrownfoxx.neon.common.model.MemberId
import com.thebrownfoxx.neon.common.model.Result
import com.thebrownfoxx.neon.common.model.UnitResult
import com.thebrownfoxx.neon.common.model.fold
import com.thebrownfoxx.neon.common.model.getOrElse
import com.thebrownfoxx.neon.common.model.map
import com.thebrownfoxx.neon.common.model.mapError
import com.thebrownfoxx.neon.common.model.onFailure
import com.thebrownfoxx.neon.server.model.Community
import com.thebrownfoxx.neon.server.model.Group
import com.thebrownfoxx.neon.server.repository.group.GroupRepository
import com.thebrownfoxx.neon.server.repository.group.model.RepositoryAddGroupError
import com.thebrownfoxx.neon.server.repository.group.model.RepositoryGetGroupError
import com.thebrownfoxx.neon.server.repository.groupmember.GroupMemberRepository
import com.thebrownfoxx.neon.server.repository.groupmember.model.RepositoryAddGroupMemberError
import com.thebrownfoxx.neon.server.repository.groupmember.model.RepositoryGetGroupMembersError
import com.thebrownfoxx.neon.server.repository.invite.InviteCodeRepository
import com.thebrownfoxx.neon.server.repository.invite.model.RepositoryGetInviteCodeGroupError
import com.thebrownfoxx.neon.server.repository.invite.model.RepositorySetInviteCodeError
import com.thebrownfoxx.neon.server.repository.member.MemberRepository
import com.thebrownfoxx.neon.server.repository.member.model.RepositoryGetMemberError
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

    override fun getGroup(id: GroupId): Flow<Result<Group, GetGroupError>> {
        return groupRepository.get(id).map { group ->
            group.mapError {
                when (it) {
                    RepositoryGetGroupError.NotFound -> GetGroupError.NotFound(id)
                    RepositoryGetGroupError.ConnectionError -> GetGroupError.ConnectionError
                }
            }
        }
    }

    override suspend fun createCommunity(
        actorId: MemberId,
        name: String,
        god: Boolean,
    ): Result<GroupId, CreateCommunityError> {
        val authorizationError = authorizeCreateCommunity(actorId)
        if (authorizationError != null) return Failure(authorizationError)

        if (name.length > maxCommunityNameLength)
            return Failure(CreateCommunityError.NameTooLong(name, maxCommunityNameLength))

        val community = Community(
            name = name,
            avatarUrl = null,
            god = god,
        )

        return groupRepository.add(community).map(
            onSuccess = { community.id },
            onFailure = {
                when (it) {
                    RepositoryAddGroupError.DuplicateId ->
                        error("Cannot add community with duplicate id")

                    RepositoryAddGroupError.ConnectionError -> CreateCommunityError.ConnectionError
                }
            },
        )
    }

    private suspend fun authorizeCreateCommunity(actorId: MemberId): CreateCommunityError? {
        val isGod = permissionChecker.isGod(actorId).getOrElse {
            return when (it) {
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
    ): UnitResult<SetInviteCodeError> {
        val authorizationError = authorizeSetInviteCode(actorId, groupId)
        if (authorizationError != null) return Failure(authorizationError)

        val inviteCodeExists = inviteCodeRepository.getGroup(inviteCode).fold(
            onSuccess = { true },
            onFailure = {
                when (it) {
                    RepositoryGetInviteCodeGroupError.NotFound -> false
                    RepositoryGetInviteCodeGroupError.ConnectionError ->
                        return Failure(SetInviteCodeError.ConnectionError)
                }
            }
        )

        if (inviteCodeExists) return Failure(SetInviteCodeError.DuplicateInviteCode(inviteCode))

        val group = groupRepository.get(groupId).first().getOrElse {
            return Failure(
                when (it) {
                    RepositoryGetGroupError.NotFound -> SetInviteCodeError.GroupNotFound(groupId)
                    RepositoryGetGroupError.ConnectionError -> SetInviteCodeError.ConnectionError
                }
            )
        }

        if (group !is Community) return Failure(SetInviteCodeError.GroupNotCommunity(groupId))

        return inviteCodeRepository.set(groupId, inviteCode).mapError {
            when (it) {
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
        val isGod = permissionChecker.isGod(actorId).getOrElse {
            return when (it) {
                IsGodError.ConnectionError -> SetInviteCodeError.ConnectionError
            }
        }

        val isGroupAdmin = permissionChecker.isGroupAdmin(groupId, actorId).getOrElse {
            return when (it) {
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
    ): UnitResult<AddGroupMemberError> {
        val authorizationError = authorizeAddMember(actorId, groupId)

        if (authorizationError != null) return Failure(authorizationError)

        groupRepository.get(groupId).first().onFailure {
            return Failure(
                when (it) {
                    RepositoryGetGroupError.NotFound -> AddGroupMemberError.GroupNotFound(groupId)
                    RepositoryGetGroupError.ConnectionError -> AddGroupMemberError.ConnectionError
                }
            )
        }

        memberRepository.get(memberId).first().onFailure {
            return Failure(
                when (it) {
                    RepositoryGetMemberError.NotFound -> AddGroupMemberError.MemberNotFound(memberId)
                    RepositoryGetMemberError.ConnectionError -> AddGroupMemberError.ConnectionError
                }
            )
        }

        val groupMembers = groupMemberRepository.getMembers(groupId).first().getOrElse {
            return Failure(
                when (it) {
                    RepositoryGetGroupMembersError.ConnectionError ->
                        AddGroupMemberError.ConnectionError
                }
            )
        }

        if (memberId in groupMembers) return Failure(AddGroupMemberError.AlreadyAMember(memberId))

        return groupMemberRepository.addMember(groupId, memberId, isAdmin).map(
            onSuccess = {},
            onFailure = {
                when (it) {
                    RepositoryAddGroupMemberError.DuplicateMembership ->
                        AddGroupMemberError.AlreadyAMember(memberId)

                    RepositoryAddGroupMemberError.ConnectionError ->
                        AddGroupMemberError.ConnectionError
                }
            },
        )
    }

    private suspend fun authorizeAddMember(
        actorId: MemberId,
        groupId: GroupId,
    ): AddGroupMemberError? {
        val isGod = permissionChecker.isGod(actorId).getOrElse {
            return when (it) {
                IsGodError.ConnectionError -> AddGroupMemberError.ConnectionError
            }
        }

        val isGroupAdmin = permissionChecker.isGroupAdmin(groupId, actorId).getOrElse {
            return when (it) {
                IsGroupAdminError.ConnectionError -> AddGroupMemberError.ConnectionError
            }
        }

        if (!(isGod) || isGroupAdmin) return AddGroupMemberError.Unauthorized(actorId)

        return null
    }
}