package com.thebrownfoxx.neon.server.model

import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.common.type.id.MessageId
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val id: MessageId = MessageId(),
    val groupId: GroupId,
    val senderId: MemberId,
    val content: String,
    val timestamp: Instant,
    val delivery: Delivery,
)

sealed interface Delivery {
    data object Sent : Delivery
    data object Delivered : Delivery
    data object Read : Delivery
}
