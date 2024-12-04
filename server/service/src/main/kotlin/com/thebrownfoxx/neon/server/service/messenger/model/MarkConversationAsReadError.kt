package com.thebrownfoxx.neon.server.service.messenger.model

import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId

sealed interface MarkConversationAsReadError {
    data class Unauthorized(val memberId: MemberId) : MarkConversationAsReadError
    data object AlreadyRead : MarkConversationAsReadError
    data class GroupNotFound(val groupId: GroupId) : MarkConversationAsReadError
    data object InternalError : MarkConversationAsReadError
}