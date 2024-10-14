package com.thebrownfoxx.neon.server.service.group.model

import com.thebrownfoxx.neon.common.model.MemberId

sealed interface CreateCommunityError {
    data class Unauthorized(val loggedInMemberId: MemberId?) : CreateCommunityError
    data class NameTooLong(val maxLength: Int) : CreateCommunityError
    data object ConnectionError : CreateCommunityError
}