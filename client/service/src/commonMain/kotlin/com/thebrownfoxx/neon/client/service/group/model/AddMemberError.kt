package com.thebrownfoxx.neon.client.service.group.model

import com.thebrownfoxx.neon.common.model.MemberId

sealed interface AddMemberError {
    data class Unauthorized(val loggedInMemberId: MemberId?) : AddMemberError
    data object GroupNotFound : AddMemberError
    data object MemberNotFound : AddMemberError
    data object ConnectionError : AddMemberError
}