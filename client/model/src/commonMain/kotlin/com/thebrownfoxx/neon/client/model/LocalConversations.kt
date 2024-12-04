package com.thebrownfoxx.neon.client.model

import com.thebrownfoxx.neon.common.type.id.GroupId

data class LocalConversations(
    val nudged: Set<GroupId>,
    val unread: Set<GroupId>,
    val read: Set<GroupId>,
)