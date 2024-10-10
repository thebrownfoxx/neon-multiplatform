package com.thebrownfoxx.neon.client.service.data.model

import com.thebrownfoxx.neon.common.model.ChatGroup
import com.thebrownfoxx.neon.common.model.Community
import com.thebrownfoxx.neon.common.model.Group
import com.thebrownfoxx.neon.common.model.MemberId

data class ChatGroupRecord(
    override val group: ChatGroup,
    override val memberIds: Set<MemberId>,
) : GroupRecord

data class CommunityRecord(
    override val group: Community,
    override val memberIds: Set<MemberId>,
) : GroupRecord

sealed interface GroupRecord {
    val group: Group
    val memberIds: Set<MemberId>
}