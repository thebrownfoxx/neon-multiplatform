package com.thebrownfoxx.neon.client.converter

import com.thebrownfoxx.neon.client.model.LocalChatGroup
import com.thebrownfoxx.neon.client.model.LocalCommunity
import com.thebrownfoxx.neon.server.model.ChatGroup
import com.thebrownfoxx.neon.server.model.Community
import com.thebrownfoxx.neon.server.model.Group

fun Group.toLocalGroup() = when (this) {
    is ChatGroup -> toLocalChatGroup()
    is Community -> toLocalCommunity()
}

fun ChatGroup.toLocalChatGroup() = LocalChatGroup(id = id)

fun Community.toLocalCommunity() = LocalCommunity(
    id = id,
    name = name,
    avatarUrl = avatarUrl,
    god = god,
)