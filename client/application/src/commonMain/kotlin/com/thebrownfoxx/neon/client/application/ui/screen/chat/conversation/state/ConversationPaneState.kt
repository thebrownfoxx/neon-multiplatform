package com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state

data class ConversationPaneState(
    val conversation: ConversationState,
    val message: String = "",
)