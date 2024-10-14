package com.thebrownfoxx.neon.server.service.member.model

import com.thebrownfoxx.neon.common.model.MemberId

sealed interface GetMemberError {
    data class Unauthorized(val loggedInMemberId: MemberId?) : GetMemberError
    data object NotFound : GetMemberError
    data object ConnectionError : GetMemberError
}