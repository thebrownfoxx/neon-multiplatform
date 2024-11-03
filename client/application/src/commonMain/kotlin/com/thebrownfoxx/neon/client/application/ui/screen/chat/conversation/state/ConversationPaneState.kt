package com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state

import com.thebrownfoxx.neon.common.type.Loadable

data class ConversationPaneState(
    val conversation: Loadable<ConversationState>,
    val message: String = "",
)