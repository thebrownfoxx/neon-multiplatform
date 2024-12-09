package com.thebrownfoxx.neon.server.service

import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.outcome.Outcome

interface PermissionChecker {
    suspend fun isGod(memberId: MemberId): Outcome<Boolean, IsGodUnexpectedError>

    suspend fun isGroupAdmin(
        groupId: GroupId,
        memberId: MemberId,
    ): Outcome<Boolean, IsGroupAdminUnexpectedError>

    data object IsGodUnexpectedError

    data object IsGroupAdminUnexpectedError
}