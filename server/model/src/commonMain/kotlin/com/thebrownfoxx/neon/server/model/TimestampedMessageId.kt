package com.thebrownfoxx.neon.server.model

import com.thebrownfoxx.neon.common.type.id.MessageId
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class TimestampedMessageId(
    val id: MessageId,
    val timestamp: Instant,
)