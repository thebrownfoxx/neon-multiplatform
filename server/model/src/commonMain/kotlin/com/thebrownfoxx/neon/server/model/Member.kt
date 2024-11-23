package com.thebrownfoxx.neon.server.model

import com.thebrownfoxx.neon.common.model.MemberId
import com.thebrownfoxx.neon.common.type.Url
import kotlinx.serialization.Serializable

@Serializable
data class Member(
    val id: MemberId = MemberId(),
    val username: String,
    val avatarUrl: Url?,
)