package com.thebrownfoxx.neon.server.service.group.model

import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId

sealed interface SetInviteCodeError {
    data class Unauthorized(val memberId: MemberId) : SetInviteCodeError
    data class GroupNotFound(val groupId: GroupId) : SetInviteCodeError
    data class GroupNotCommunity(val groupId: GroupId) : SetInviteCodeError
    data class DuplicateInviteCode(val inviteCode: String) : SetInviteCodeError
    data object ConnectionError : SetInviteCodeError
}