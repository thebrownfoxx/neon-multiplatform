package com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state

import com.thebrownfoxx.neon.client.application.ui.component.avatar.state.AvatarState

data class ConversationInfoState(
    val avatar: AvatarState?,
    val name: String?,
)