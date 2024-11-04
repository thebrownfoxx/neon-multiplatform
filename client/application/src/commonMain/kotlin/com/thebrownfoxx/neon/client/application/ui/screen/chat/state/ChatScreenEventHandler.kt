package com.thebrownfoxx.neon.client.application.ui.screen.chat.state

import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.ConversationPaneEventHandler
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ChatPreviewsEventHandler

data class ChatScreenEventHandler(
    val chatPreviewsEventHandler: ChatPreviewsEventHandler,
    val conversationPaneEventHandler: ConversationPaneEventHandler,
) {
    companion object {
        val Blank = ChatScreenEventHandler(
            chatPreviewsEventHandler = ChatPreviewsEventHandler.Blank,
            conversationPaneEventHandler = ConversationPaneEventHandler.Blank,
        )
    }
}