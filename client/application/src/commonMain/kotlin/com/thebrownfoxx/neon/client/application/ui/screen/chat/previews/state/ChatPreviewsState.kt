package com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state

data class ChatPreviewsState(
    val nudgedConversations: List<ChatPreviewState>,
    val unreadConversations: List<ChatPreviewState>,
    val readConversations: List<ChatPreviewState>,
)
