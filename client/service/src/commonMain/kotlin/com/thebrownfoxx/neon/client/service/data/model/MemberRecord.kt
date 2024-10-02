package com.thebrownfoxx.neon.client.service.data.model

import com.thebrownfoxx.neon.common.model.Member

data class MemberRecord(
    val member: Member,
    val password: String,
)