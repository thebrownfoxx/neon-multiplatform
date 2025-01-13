package com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state

class ChatPreviewsEventHandler(
    val onConversationClick: (ChatPreviewState) -> Unit,
    val onLastVisiblePreviewChange: (ChatPreviewStateId) -> Unit,
) {
    companion object {
        val Blank = ChatPreviewsEventHandler(
            onConversationClick = {},
            onLastVisiblePreviewChange = {},
        )
    }
}