package com.thebrownfoxx.neon.client.service.data.model

import com.thebrownfoxx.neon.common.model.Message

data class ServiceData(
    val memberRecords: List<MemberRecord>,
    val groupRecords: List<GroupRecord>,
    val messages: List<Message>,
)