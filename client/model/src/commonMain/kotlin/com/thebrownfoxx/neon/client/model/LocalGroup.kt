package com.thebrownfoxx.neon.client.model

import com.thebrownfoxx.neon.common.type.Url
import com.thebrownfoxx.neon.common.type.id.GroupId
import kotlinx.serialization.Serializable

@Serializable
data class LocalChatGroup(
    override val id: GroupId = GroupId(),
) : LocalGroup

@Serializable
data class LocalCommunity(
    override val id: GroupId = GroupId(),
    val name: String,
    val avatarUrl: Url?,
    val isGod: Boolean,
) : LocalGroup

sealed interface LocalGroup {
    val id: GroupId
}