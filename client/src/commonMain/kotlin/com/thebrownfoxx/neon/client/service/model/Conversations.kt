package com.thebrownfoxx.neon.client.service.model

import com.thebrownfoxx.neon.common.model.GroupId

data class Conversations(
    val nudgedConversations: List<GroupId>,
    val unreadConversations: List<GroupId>,
    val readConversations: List<GroupId>,
)