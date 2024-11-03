package com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state

import com.thebrownfoxx.neon.client.application.ui.component.delivery.state.DeliveryState
import com.thebrownfoxx.neon.common.model.MessageId
import kotlinx.datetime.LocalDateTime

data class MessageState(
    val id: MessageId = MessageId(),
    val content: String,
    val timestamp: LocalDateTime,
    val deliveryState: DeliveryState,
    val groupPosition: GroupPosition,
    val sender: MessageSenderState,
)