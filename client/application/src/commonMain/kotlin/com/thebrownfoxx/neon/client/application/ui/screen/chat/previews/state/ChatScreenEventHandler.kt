package com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state

import com.thebrownfoxx.neon.common.type.id.GroupId

class ChatPreviewsEventHandler(
    val onConversationClick: (GroupId) -> Unit,
    val onLastVisiblePreviewChange: (ChatPreviewStateId) -> Unit,
) {
    companion object {
        val Blank = ChatPreviewsEventHandler(
            onConversationClick = {},
            onLastVisiblePreviewChange = {},
        )
    }
}