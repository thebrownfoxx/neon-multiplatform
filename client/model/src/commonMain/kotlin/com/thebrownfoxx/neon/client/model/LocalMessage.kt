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
    val delivery: LocalDelivery,
)

sealed interface LocalDelivery {
    data object Sending: LocalDelivery
    data object Sent : LocalDelivery
    data object Delivered : LocalDelivery
    data object Read : LocalDelivery
    data object Failed : LocalDelivery
}
