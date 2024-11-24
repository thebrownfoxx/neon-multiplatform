package com.thebrownfoxx.neon.server.service.default

import com.thebrownfoxx.neon.common.type.Failure
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.common.type.Outcome
import com.thebrownfoxx.neon.common.type.Success
import com.thebrownfoxx.neon.common.type.getOrElse
import com.thebrownfoxx.neon.server.model.Community
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
    override suspend fun isGod(memberId: MemberId): Outcome<Boolean, IsGodError> {
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
    ): Outcome<Boolean, IsGroupAdminError> {
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