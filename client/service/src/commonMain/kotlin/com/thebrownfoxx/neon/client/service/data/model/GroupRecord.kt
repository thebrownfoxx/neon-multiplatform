package com.thebrownfoxx.neon.client.service.data.model

import com.thebrownfoxx.neon.common.model.Group
import com.thebrownfoxx.neon.common.model.MemberId
import com.thebrownfoxx.neon.common.model.MessageId

data class GroupRecord(
    val group: Group,
    val memberIds: List<MemberId>,
    val messageIds: List<MessageId>? = null,
)