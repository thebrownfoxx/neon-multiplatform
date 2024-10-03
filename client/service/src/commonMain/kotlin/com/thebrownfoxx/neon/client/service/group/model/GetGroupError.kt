package com.thebrownfoxx.neon.client.service.group.model

import com.thebrownfoxx.neon.client.service.member.model.GetMemberError
import com.thebrownfoxx.neon.common.model.MemberId

sealed interface GetGroupError {
    data class Unauthorized(val loggedInMemberId: MemberId?) : GetGroupError
    data object NotFound : GetGroupError
    data object ConnectionError : GetGroupError
}