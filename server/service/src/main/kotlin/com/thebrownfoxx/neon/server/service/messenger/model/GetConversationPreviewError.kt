package com.thebrownfoxx.neon.server.service.messenger.model

import com.thebrownfoxx.neon.common.model.MemberId

sealed interface GetConversationPreviewError {
    data class Unauthorized(val loggedInMemberId: MemberId?) : GetConversationPreviewError
    data object GroupNotFound : GetConversationPreviewError
    data object ConnectionError : GetConversationPreviewError
}