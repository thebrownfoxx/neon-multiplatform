package com.thebrownfoxx.neon.client.model

import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MessageId
import kotlinx.datetime.Instant

data class LocalTimestampedMessageId(
    val id: MessageId,
    val groupId: GroupId,
    val timestamp: Instant,
)