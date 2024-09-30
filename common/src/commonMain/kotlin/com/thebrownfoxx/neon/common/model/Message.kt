package com.thebrownfoxx.neon.common.model

import com.thebrownfoxx.neon.common.type.Id
import com.thebrownfoxx.neon.common.type.Uuid
import kotlinx.datetime.Instant

data class Message(
    val id: MessageId,
    val group: GroupId,
    val sender: MemberId,
    val content: String,
    val timestamp: Instant,
    val delivery: Delivery,
)

data class MessageId(override val uuid: Uuid = Uuid()) : Id

sealed interface Delivery {
    data object Sending: Delivery
    data object Sent : Delivery
    data object Delivered : Delivery
    data object Read : Delivery
    data object Failed : Delivery
}
