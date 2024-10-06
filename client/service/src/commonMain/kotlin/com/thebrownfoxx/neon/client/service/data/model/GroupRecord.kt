package com.thebrownfoxx.neon.client.service.data.model

import com.thebrownfoxx.neon.common.model.ChatGroup
import com.thebrownfoxx.neon.common.model.Community
import com.thebrownfoxx.neon.common.model.Group
import com.thebrownfoxx.neon.common.model.MemberId
import com.thebrownfoxx.neon.common.model.MessageId

data class ChatGroupRecord(
    override val group: ChatGroup,
    override val memberIds: List<MemberId>,
    override val messageIds: List<MessageId>? = null,
) : GroupRecord

data class CommunityRecord(
    override val group: Community,
    override val memberIds: List<MemberId>,
    override val messageIds: List<MessageId>? = null,
) : GroupRecord

sealed interface GroupRecord {
    val group: Group
    val memberIds: List<MemberId>
    val messageIds: List<MessageId>?
}