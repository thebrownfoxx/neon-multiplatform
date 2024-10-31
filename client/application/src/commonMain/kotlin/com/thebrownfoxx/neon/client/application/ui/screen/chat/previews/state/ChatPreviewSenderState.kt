package com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state

import com.thebrownfoxx.neon.client.application.ui.component.avatar.state.SingleAvatarState

sealed interface ChatPreviewSenderState

data object SentState : ChatPreviewSenderState

data object ReceivedDirectState : ChatPreviewSenderState

data class ReceivedCommunityState(val senderAvatar: SingleAvatarState) : ChatPreviewSenderState