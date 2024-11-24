package com.thebrownfoxx.neon.server.model

import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.IgnoredUuid
import com.thebrownfoxx.neon.common.type.Url
import kotlinx.serialization.Serializable

@Serializable
data class ChatGroup(
    override val id: GroupId = GroupId(),
) : Group {
    override fun ignoreId() = copy(id = ignoredGroupId)
}

@Serializable
data class Community(
    override val id: GroupId = GroupId(),
    val name: String,
    val avatarUrl: Url?,
    val god: Boolean,
) : Group {
    override fun ignoreId() = copy(id = ignoredGroupId)
}

sealed interface Group {
    val id: GroupId

    fun ignoreId(): Group
}

private val ignoredGroupId = GroupId(IgnoredUuid)