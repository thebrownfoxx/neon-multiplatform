package com.thebrownfoxx.neon.client.application.ui.screen.conversations.state

import com.thebrownfoxx.neon.client.application.ui.component.avatar.state.SingleAvatarState

sealed interface SenderState

data object SentBySelfState : SenderState

data class SentByOtherState(val avatar: SingleAvatarState) : SenderState