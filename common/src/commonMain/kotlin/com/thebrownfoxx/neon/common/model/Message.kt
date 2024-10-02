package com.thebrownfoxx.neon.common.model

import com.thebrownfoxx.neon.common.type.Id
import com.thebrownfoxx.neon.common.type.Uuid
import kotlinx.datetime.Instant

data class Message(
    val id: MessageId = MessageId(),
    val groupId: GroupId,
    val senderId: MemberId,
    val content: String,
    val timestamp: Instant,
    val delivery: Delivery,
) {
    fun ignoreId(): Message = copy(id = ignoredMessageId)
}

private val ignoredMessageId = MessageId(Uuid("IGNORED"))

data class MessageId(override val uuid: Uuid = Uuid()) : Id

sealed interface Delivery {
    data object Sending: Delivery
    data object Sent : Delivery
    data object Delivered : Delivery
    data object Read : Delivery
    data object Failed : Delivery
}
