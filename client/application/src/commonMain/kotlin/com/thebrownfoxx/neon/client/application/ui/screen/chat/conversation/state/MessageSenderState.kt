package com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state

import com.thebrownfoxx.neon.client.application.ui.component.avatar.state.SingleAvatarState

sealed interface MessageSenderState

data object SentState : MessageSenderState

data object ReceivedDirectState : MessageSenderState

data class ReceivedCommunityState(val senderAvatar: SingleAvatarState) : MessageSenderState