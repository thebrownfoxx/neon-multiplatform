package com.thebrownfoxx.neon.client.converter

import com.thebrownfoxx.neon.client.model.LocalTimestampedMessageId
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.server.model.TimestampedMessageId

fun TimestampedMessageId.toLocalTimestampedMessageId(groupId: GroupId) = LocalTimestampedMessageId(
    id = id,
    groupId = groupId,
    timestamp = timestamp,
)