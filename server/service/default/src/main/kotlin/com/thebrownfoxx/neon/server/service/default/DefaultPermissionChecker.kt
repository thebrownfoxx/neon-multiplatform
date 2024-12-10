package com.thebrownfoxx.neon.server.service.default

import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.server.model.Community
import com.thebrownfoxx.neon.server.repository.GroupMemberRepository
import com.thebrownfoxx.neon.server.service.PermissionChecker
import com.thebrownfoxx.neon.server.service.PermissionChecker.IsGodUnexpectedError
import com.thebrownfoxx.neon.server.service.PermissionChecker.IsGroupAdminUnexpectedError
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.Success
import com.thebrownfoxx.outcome.map.getOrElse

class DefaultPermissionChecker(
    private val groupMemberRepository: GroupMemberRepository,
) : PermissionChecker {
    override suspend fun isGod(memberId: MemberId): Outcome<Boolean, IsGodUnexpectedError> {
        val communities = groupMemberRepository.getGroups(memberId).getOrElse {
            return Failure(IsGodUnexpectedError)
        }.filterIsInstance<Community>()
        return Success(communities.any { it.isGod })
    }

    override suspend fun isGroupAdmin(
        groupId: GroupId,
        memberId: MemberId,
    ): Outcome<Boolean, IsGroupAdminUnexpectedError> {
        val admins = groupMemberRepository.getAdmins(groupId).getOrElse {
            return Failure(IsGroupAdminUnexpectedError)
        }
        return Success(memberId in admins)
    }
}