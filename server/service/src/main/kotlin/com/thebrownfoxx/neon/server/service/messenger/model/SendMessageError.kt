package com.thebrownfoxx.neon.server.service.messenger.model

import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId

sealed interface SendMessageError {
    data class Unauthorized(val memberId: MemberId) : SendMessageError
    data class GroupNotFound(val groupId: GroupId) : SendMessageError
    data object ConnectionError : SendMessageError
}