package com.thebrownfoxx.neon.server.service.messenger.model

import com.thebrownfoxx.neon.common.type.id.MemberId

sealed interface GetConversationsError {
    data class MemberNotFound(val memberId: MemberId) : GetConversationsError
    data object ConnectionError : GetConversationsError
}