package com.thebrownfoxx.neon.server.service.messenger.model

import com.thebrownfoxx.neon.common.model.MemberId

sealed interface GetMessageError {
    data class Unauthorized(val loggedInMemberId: MemberId?) : GetMessageError
    data object NotFound : GetMessageError
    data object ConnectionError : GetMessageError
}