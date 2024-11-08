package com.thebrownfoxx.neon.server.service.messenger.model

import com.thebrownfoxx.neon.common.model.GroupId
import com.thebrownfoxx.neon.common.model.MemberId

sealed interface MarkConversationAsReadError {
    data class Unauthorized(val memberId: MemberId) : MarkConversationAsReadError
    data object AlreadyRead : MarkConversationAsReadError
    data class GroupNotFound(val groupId: GroupId) : MarkConversationAsReadError
    data object ConnectionError : MarkConversationAsReadError
}