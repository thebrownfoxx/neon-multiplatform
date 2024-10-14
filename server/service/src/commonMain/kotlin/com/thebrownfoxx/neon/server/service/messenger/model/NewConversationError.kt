package com.thebrownfoxx.neon.server.service.messenger.model

import com.thebrownfoxx.neon.common.model.MemberId

sealed interface NewConversationError {
    data class Unauthorized(val loggedInMemberId: MemberId?) : SendMessageError
    data class MemberNotFound(val memberId: MemberId) : SendMessageError
    data object ConnectionError : SendMessageError
}