package com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state

import com.thebrownfoxx.neon.client.application.ui.component.avatar.state.SingleAvatarState

sealed interface ChatPreviewSenderState

data object SentChatPreviewState : ChatPreviewSenderState

data object ReceivedDirectChatPreviewState : ChatPreviewSenderState

data class ReceivedCommunityChatPreviewState(
    val senderAvatar: SingleAvatarState,
) : ChatPreviewSenderState