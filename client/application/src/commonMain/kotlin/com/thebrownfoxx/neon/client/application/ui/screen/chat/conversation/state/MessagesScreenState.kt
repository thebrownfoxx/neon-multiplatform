package com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state

data class MessagesScreenState(
    val messages: ConversationState,
    val message: String = "",
)