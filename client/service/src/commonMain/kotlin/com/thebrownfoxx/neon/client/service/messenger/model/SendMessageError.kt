package com.thebrownfoxx.neon.client.service.messenger.model

import com.thebrownfoxx.neon.common.model.MemberId

sealed interface SendMessageError {
    data class Unauthorized(val loggedInMemberId: MemberId?) : SendMessageError
    data object GroupNotFound : SendMessageError
    data object ConnectionError : SendMessageError
}