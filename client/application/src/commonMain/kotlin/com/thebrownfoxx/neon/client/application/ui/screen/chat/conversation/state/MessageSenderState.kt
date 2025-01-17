package com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state

import com.thebrownfoxx.neon.client.application.ui.component.avatar.state.SingleAvatarState

sealed interface MessageSenderState

data object SentMessageState : MessageSenderState

data object ReceivedDirectMessageState : MessageSenderState

data class ReceivedGroupMessageState(
    val senderAvatar: SingleAvatarState,
) : MessageSenderState