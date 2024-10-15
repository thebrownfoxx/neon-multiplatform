package com.thebrownfoxx.neon.server.service.permission

import com.thebrownfoxx.neon.common.model.GroupId
import com.thebrownfoxx.neon.common.model.MemberId

interface PermissionChecker {
    fun isGod(memberId: MemberId): Boolean
    fun isGroupAdmin(groupId: GroupId, memberId: MemberId): Boolean
}