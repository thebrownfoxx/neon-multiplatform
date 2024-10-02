package com.thebrownfoxx.neon.common.model

import com.thebrownfoxx.neon.common.type.Id
import com.thebrownfoxx.neon.common.type.IgnoredUuid
import com.thebrownfoxx.neon.common.type.Url
import com.thebrownfoxx.neon.common.type.Uuid
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
) : Group {
    override fun ignoreId() = copy(id = ignoredGroupId)
}

sealed interface Group {
    val id: GroupId

    fun ignoreId(): Group
}

private val ignoredGroupId = GroupId(IgnoredUuid)

@Serializable
data class GroupId(override val uuid: Uuid = Uuid()) : Id