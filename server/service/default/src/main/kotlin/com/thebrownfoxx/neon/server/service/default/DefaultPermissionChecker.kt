package com.thebrownfoxx.neon.server.service.default

import com.thebrownfoxx.neon.common.outcome.Failure
import com.thebrownfoxx.neon.common.outcome.Outcome
import com.thebrownfoxx.neon.common.outcome.Success
import com.thebrownfoxx.neon.common.outcome.getOrElse
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.server.model.Community
import com.thebrownfoxx.neon.server.repository.GroupMemberRepository
import com.thebrownfoxx.neon.server.service.permission.PermissionChecker
import com.thebrownfoxx.neon.server.service.permission.model.IsGodError
import com.thebrownfoxx.neon.server.service.permission.model.IsGroupAdminError
import kotlinx.coroutines.flow.first

class DefaultPermissionChecker(
    private val groupMemberRepository: GroupMemberRepository,
) : PermissionChecker {
    override suspend fun isGod(memberId: MemberId): Outcome<Boolean, IsGodError> {
        val communities = groupMemberRepository.getGroupsAsFlow(memberId).first().getOrElse {
            return Failure(IsGodError.ConnectionError)
        }.filterIsInstance<Community>()

        return Success(communities.any { it.isGod })
    }

    override suspend fun isGroupAdmin(
        groupId: GroupId,
        memberId: MemberId,
    ): Outcome<Boolean, IsGroupAdminError> {
        val admins = groupMemberRepository.getAdminsAsFlow(groupId).first().getOrElse {
            return Failure(IsGroupAdminError.ConnectionError)
        }

        return Success(memberId in admins)
    }
}