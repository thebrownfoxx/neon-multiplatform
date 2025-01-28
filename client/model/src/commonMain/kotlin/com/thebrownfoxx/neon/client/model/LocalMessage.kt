package com.thebrownfoxx.neon.client.model

import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.common.type.id.MessageId
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class LocalMessage(
    val id: MessageId = MessageId(),
    val groupId: GroupId,
    val senderId: MemberId,
    val content: String,
    val timestamp: Instant,

    @Deprecated("Use delivery for each member")
    val delivery: LocalDelivery,
)

enum class LocalDelivery {
    Sending,
    Sent,
    Delivered,
    Read,
    Failed,
}
