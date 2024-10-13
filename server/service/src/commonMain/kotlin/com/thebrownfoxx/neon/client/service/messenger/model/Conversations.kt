package com.thebrownfoxx.neon.client.service.messenger.model

import com.thebrownfoxx.neon.common.model.GroupId

data class Conversations(
    val nudgedGroupIds: Set<GroupId>,
    val unreadGroupIds: Set<GroupId>,
    val readGroupIds: Set<GroupId>,
)