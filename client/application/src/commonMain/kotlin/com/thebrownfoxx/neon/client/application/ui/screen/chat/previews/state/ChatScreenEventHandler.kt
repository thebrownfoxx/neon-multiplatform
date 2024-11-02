package com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state

class ChatPreviewsEventHandler(
    val onLoadMore: () -> Unit,
    val onConversationClick: (ChatPreviewState) -> Unit,
) {
    companion object {
        val Blank = ChatPreviewsEventHandler(
            onLoadMore = {},
            onConversationClick = {},
        )
    }
}