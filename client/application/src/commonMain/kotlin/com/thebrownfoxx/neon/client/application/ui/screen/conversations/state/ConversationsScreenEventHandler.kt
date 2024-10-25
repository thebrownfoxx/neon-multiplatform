package com.thebrownfoxx.neon.client.application.ui.screen.conversations.state

class ConversationPreviewsScreenEventHandler(
    val onLoadMore: () -> Unit,
    val onConversationClick: (ConversationPreviewState) -> Unit,
) {
    companion object {
        val Blank = ConversationPreviewsScreenEventHandler(
            onLoadMore = {},
            onConversationClick = {},
        )
    }
}