package com.thebrownfoxx.neon.server.repository.data.model

import com.thebrownfoxx.neon.common.model.MemberId
import com.thebrownfoxx.neon.server.model.ChatGroup
import com.thebrownfoxx.neon.server.model.Community
import com.thebrownfoxx.neon.server.model.Group

data class ChatGroupRecord(
    override val group: ChatGroup,
    override val memberIds: Set<MemberId>,
) : GroupRecord

data class CommunityRecord(
    override val group: Community,
    override val memberIds: Set<MemberId>,
    val inviteCode: String?,
) : GroupRecord

sealed interface GroupRecord {
    val group: Group
    val memberIds: Set<MemberId>

    operator fun component1() = group
    operator fun component2() = memberIds
}