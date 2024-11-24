package com.thebrownfoxx.neon.server.service.group.model

import com.thebrownfoxx.neon.common.type.id.GroupId

sealed interface GetGroupError {
    data class NotFound(val groupId: GroupId) : GetGroupError
    data object ConnectionError : GetGroupError
}