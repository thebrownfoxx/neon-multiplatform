package com.thebrownfoxx.neon.client.application.ui.screen.conversations.state

import com.thebrownfoxx.neon.client.application.ui.component.delivery.state.DeliveryState
import kotlinx.datetime.LocalDateTime

data class PreviewContentState(
    val message: String,
    val timestamp: LocalDateTime,
    val delivery: DeliveryState?,
    val senderState: SenderState,
)