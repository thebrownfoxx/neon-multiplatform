package com.thebrownfoxx.neon.common.model

import com.thebrownfoxx.neon.common.type.Id
import com.thebrownfoxx.neon.common.type.IgnoredUuid
import com.thebrownfoxx.neon.common.type.Url
import com.thebrownfoxx.neon.common.type.Uuid
import kotlinx.serialization.Serializable

@Serializable
data class Member(
    val id: MemberId = MemberId(),
    val username: String,
    val avatarUrl: Url?,
) {
    fun ignoreId(): Member = copy(id = ignoredMemberId)
}

private val ignoredMemberId = MemberId(IgnoredUuid)

@Serializable
data class MemberId(override val uuid: Uuid = Uuid()) : Id
