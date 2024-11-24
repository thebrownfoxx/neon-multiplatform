package com.thebrownfoxx.neon.server.service.group.model

import com.thebrownfoxx.neon.common.type.id.MemberId

sealed interface CreateCommunityError {
    data class Unauthorized(val memberId: MemberId) : CreateCommunityError
    data class NameTooLong(val name: String, val maxLength: Int) : CreateCommunityError
    data object ConnectionError : CreateCommunityError
}