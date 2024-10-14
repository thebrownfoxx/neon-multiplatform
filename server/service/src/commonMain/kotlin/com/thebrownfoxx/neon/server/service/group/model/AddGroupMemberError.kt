package com.thebrownfoxx.neon.server.service.group.model

import com.thebrownfoxx.neon.common.model.MemberId

sealed interface AddGroupMemberError {
    data class Unauthorized(val loggedInMemberId: MemberId?) : AddGroupMemberError
    data object GroupNotFound : AddGroupMemberError
    data object MemberNotFound : AddGroupMemberError
    data object ConnectionError : AddGroupMemberError
}