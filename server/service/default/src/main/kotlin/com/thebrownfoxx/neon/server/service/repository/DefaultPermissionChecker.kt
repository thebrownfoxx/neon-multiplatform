package com.thebrownfoxx.neon.server.service.repository

import com.thebrownfoxx.neon.common.model.Community
import com.thebrownfoxx.neon.common.model.Failure
import com.thebrownfoxx.neon.common.model.GroupId
import com.thebrownfoxx.neon.common.model.MemberId
import com.thebrownfoxx.neon.common.model.Result
import com.thebrownfoxx.neon.common.model.Success
import com.thebrownfoxx.neon.common.model.getOrElse
import com.thebrownfoxx.neon.server.repository.groupmember.GroupMemberRepository
import com.thebrownfoxx.neon.server.repository.groupmember.model.RepositoryGetAdminsError
import com.thebrownfoxx.neon.server.repository.groupmember.model.RepositoryGetMemberGroupsError
import com.thebrownfoxx.neon.server.service.permission.PermissionChecker
import com.thebrownfoxx.neon.server.service.permission.model.IsGodError
import com.thebrownfoxx.neon.server.service.permission.model.IsGroupAdminError
import kotlinx.coroutines.flow.first

class DefaultPermissionChecker(
    private val groupMemberRepository: GroupMemberRepository,
) : PermissionChecker {
    override suspend fun isGod(memberId: MemberId): Result<Boolean, IsGodError> {
        val communities = groupMemberRepository.getGroups(memberId).first().getOrElse {
            return Failure(
                when (it) {
                    RepositoryGetMemberGroupsError.ConnectionError -> IsGodError.ConnectionError
                }
            )
        }.filterIsInstance<Community>()

        return Success(communities.any { it.god })
    }

    override suspend fun isGroupAdmin(
        groupId: GroupId,
        memberId: MemberId,
    ): Result<Boolean, IsGroupAdminError> {
        val admins = groupMemberRepository.getAdmins(groupId).first().getOrElse {
            return Failure(
                when (it) {
                    RepositoryGetAdminsError.ConnectionError -> IsGroupAdminError.ConnectionError
                }
            )
        }

        return Success(memberId in admins)
    }
}