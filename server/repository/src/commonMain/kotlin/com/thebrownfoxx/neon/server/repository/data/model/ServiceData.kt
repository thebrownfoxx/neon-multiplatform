package com.thebrownfoxx.neon.server.repository.data.model

import com.thebrownfoxx.neon.server.model.Message

data class ServiceData(
    val memberRecords: List<MemberRecord>,
    val groupRecords: List<GroupRecord>,
    val messages: List<Message>,
)