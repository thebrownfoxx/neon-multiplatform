package com.thebrownfoxx.neon.server.service.messenger.model

import com.thebrownfoxx.neon.common.model.GroupId
import com.thebrownfoxx.neon.common.model.MemberId

sealed interface GetMessagesError {
    data class Unauthorized(val memberId: MemberId) : GetMessagesError
    data class GroupNotFound(val groupId: GroupId) : GetMessagesError
    data object ConnectionError : GetMessagesError
}