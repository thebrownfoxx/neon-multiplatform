package com.thebrownfoxx.neon.server.service.messenger.model

import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.common.type.id.MessageId

sealed interface GetMessageError {
    data class Unauthorized(val memberId: MemberId) : GetMessageError
    data class NotFound(val messageId: MessageId) : GetMessageError
    data object ConnectionError : GetMessageError
}