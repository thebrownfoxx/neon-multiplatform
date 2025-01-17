package com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state

data class ChatPreviewsState(
    val listItems: List<ChatPreviewListItem>,
    val ready: Boolean,
)