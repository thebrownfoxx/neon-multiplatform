package com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state

class ConversationPaneEventHandler(
    val onCall: () -> Unit,
    val onMarkAsRead: () -> Unit,
    val onMessageChange: (String) -> Unit,
    val onSend: () -> Unit,
    val onClose: () -> Unit,
    val onLastVisibleEntryChange: (MessageListEntryId) -> Unit,
) {
    companion object {
        val Blank = ConversationPaneEventHandler(
            onCall = {},
            onMarkAsRead = {},
            onMessageChange = {},
            onSend = {},
            onClose = {},
            onLastVisibleEntryChange = {},
        )
    }
}