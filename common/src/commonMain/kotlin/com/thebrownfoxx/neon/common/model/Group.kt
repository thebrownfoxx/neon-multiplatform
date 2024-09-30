package com.thebrownfoxx.neon.common.model

import com.thebrownfoxx.neon.common.type.Id
import com.thebrownfoxx.neon.common.type.Url
import com.thebrownfoxx.neon.common.type.Uuid
import kotlinx.serialization.Serializable

@Serializable
data class ChatGroup(
    override val id: GroupId = GroupId(),
) : Group

@Serializable
data class Community(
    override val id: GroupId = GroupId(),
    val name: String,
    val avatarUrl: Url?,
) : Group

sealed interface Group {
    val id: GroupId
}

@Serializable
data class GroupId(override val uuid: Uuid = Uuid()) : Id