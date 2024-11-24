package com.thebrownfoxx.neon.client.model

import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.Url
import kotlinx.serialization.Serializable

@Serializable
data class ChatLocalGroup(
    override val id: GroupId = GroupId(),
) : LocalGroup

@Serializable
data class LocalCommunity(
    override val id: GroupId = GroupId(),
    val name: String,
    val avatarUrl: Url?,
    val god: Boolean,
) : LocalGroup

sealed interface LocalGroup {
    val id: GroupId
}