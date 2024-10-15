package com.thebrownfoxx.neon.server.service.group.model

import com.thebrownfoxx.neon.common.model.GroupId
import com.thebrownfoxx.neon.common.model.MemberId

sealed interface AddGroupMemberError {
    data class Unauthorized(val memberId: MemberId) : AddGroupMemberError
    data class AlreadyAMember(val memberId: MemberId) : AddGroupMemberError
    data class GroupNotFound(val groupId: GroupId) : AddGroupMemberError
    data class MemberNotFound(val memberId: MemberId) : AddGroupMemberError
    data object ConnectionError : AddGroupMemberError
}