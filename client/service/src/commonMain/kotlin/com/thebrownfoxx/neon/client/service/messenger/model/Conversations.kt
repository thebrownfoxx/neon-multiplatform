package com.thebrownfoxx.neon.client.service.messenger.model

import com.thebrownfoxx.neon.common.model.GroupId

data class Conversations(
    val nudgedGroupIds: List<GroupId>,
    val unreadGroupIds: List<GroupId>,
    val readGroupIds: List<GroupId>,
)