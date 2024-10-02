package com.thebrownfoxx.neon.client.service.data

data class ServiceData(
    val members: List<MemberData>,
    val groups: List<GroupData>,
    val conversations: List<Conversation>,
)