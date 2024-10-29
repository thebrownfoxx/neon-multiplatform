package com.thebrownfoxx.neon.client.application.ui.screen.conversations.state

import com.thebrownfoxx.neon.client.application.ui.component.avatar.state.SingleAvatarState

sealed interface SenderState

data object SentState : SenderState

data object ReceivedDirectState : SenderState

data class ReceivedCommunityState(val senderAvatar: SingleAvatarState) : SenderState