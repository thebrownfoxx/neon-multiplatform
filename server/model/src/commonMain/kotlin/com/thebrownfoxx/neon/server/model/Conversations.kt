package com.thebrownfoxx.neon.server.model

import com.thebrownfoxx.neon.common.type.id.GroupId
import kotlinx.serialization.Serializable

@Serializable
data class Conversations(
    val nudgedGroupIds: Set<GroupId>,
    val unreadGroupIds: Set<GroupId>,
    val readGroupIds: Set<GroupId>,
)