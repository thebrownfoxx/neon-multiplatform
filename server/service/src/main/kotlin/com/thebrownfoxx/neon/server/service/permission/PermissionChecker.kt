package com.thebrownfoxx.neon.server.service.permission

import com.thebrownfoxx.neon.common.model.GroupId
import com.thebrownfoxx.neon.common.model.MemberId
import com.thebrownfoxx.neon.common.model.Result
import com.thebrownfoxx.neon.server.service.permission.model.IsGodError
import com.thebrownfoxx.neon.server.service.permission.model.IsGroupAdminError

interface PermissionChecker {
    suspend fun isGod(memberId: MemberId): Result<Boolean, IsGodError>
    suspend fun isGroupAdmin(
        groupId: GroupId,
        memberId: MemberId,
    ): Result<Boolean, IsGroupAdminError>
}