package com.thebrownfoxx.neon.server.service.messenger.model

import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId

sealed interface GetMessagesError {
    data class Unauthorized(val memberId: MemberId) : GetMessagesError
    data class GroupNotFound(val groupId: GroupId) : GetMessagesError
    data object ConnectionError : GetMessagesError
}