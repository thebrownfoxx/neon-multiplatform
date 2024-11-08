package com.thebrownfoxx.neon.server.service.messenger.model

import com.thebrownfoxx.neon.common.model.GroupId
import com.thebrownfoxx.neon.common.model.MemberId

sealed interface GetConversationPreviewError {
    data class Unauthorized(val memberId: MemberId) : GetConversationPreviewError
    data class GroupNotFound(val groupId: GroupId) : GetConversationPreviewError
    data object ConnectionError : GetConversationPreviewError
}