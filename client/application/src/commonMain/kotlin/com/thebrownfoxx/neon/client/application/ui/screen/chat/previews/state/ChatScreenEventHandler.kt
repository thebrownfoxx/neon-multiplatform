package com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state

class ChatPreviewsScreenEventHandler(
    val onLoadMore: () -> Unit,
    val onConversationClick: (ChatPreviewState) -> Unit,
) {
    companion object {
        val Blank = ChatPreviewsScreenEventHandler(
            onLoadMore = {},
            onConversationClick = {},
        )
    }
}