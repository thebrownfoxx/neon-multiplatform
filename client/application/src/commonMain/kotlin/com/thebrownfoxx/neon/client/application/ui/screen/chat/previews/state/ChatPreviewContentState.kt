package com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state

import com.thebrownfoxx.neon.client.application.ui.component.delivery.state.DeliveryState
import kotlinx.datetime.LocalDateTime

data class ChatPreviewContentState(
    val message: String,
    val timestamp: LocalDateTime,
    val delivery: DeliveryState?,
    val senderState: SenderState,
)