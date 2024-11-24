package com.thebrownfoxx.neon.server.service.permission

import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.common.type.Outcome
import com.thebrownfoxx.neon.server.service.permission.model.IsGodError
import com.thebrownfoxx.neon.server.service.permission.model.IsGroupAdminError

interface PermissionChecker {
    suspend fun isGod(memberId: MemberId): Outcome<Boolean, IsGodError>
    suspend fun isGroupAdmin(
        groupId: GroupId,
        memberId: MemberId,
    ): Outcome<Boolean, IsGroupAdminError>
}