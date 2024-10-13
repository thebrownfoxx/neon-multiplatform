package com.thebrownfoxx.neon.client.service.messenger.model

import com.thebrownfoxx.neon.common.model.MemberId

sealed interface GetMessagesError {
    data class Unauthorized(val loggedInMemberId: MemberId?) : GetMessagesError
    data object GroupNotFound : GetMessagesError
    data object ConnectionError : GetMessagesError
}