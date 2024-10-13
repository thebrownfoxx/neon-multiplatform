package com.thebrownfoxx.neon.client.service.messenger.model

import com.thebrownfoxx.neon.common.model.MemberId

sealed interface MarkConversationAsReadError {
    data class Unauthorized(val loggedInMemberId: MemberId?) : MarkConversationAsReadError
    data object AlreadyRead : MarkConversationAsReadError
    data object GroupNotFound : MarkConversationAsReadError
    data object ConnectionError : MarkConversationAsReadError
}