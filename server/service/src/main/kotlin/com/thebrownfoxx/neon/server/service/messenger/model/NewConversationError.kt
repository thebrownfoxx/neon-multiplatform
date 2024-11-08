package com.thebrownfoxx.neon.server.service.messenger.model

import com.thebrownfoxx.neon.common.model.MemberId

sealed interface NewConversationError {
    data class MemberNotFound(val memberId: MemberId) : NewConversationError
    data object ConnectionError : NewConversationError
}