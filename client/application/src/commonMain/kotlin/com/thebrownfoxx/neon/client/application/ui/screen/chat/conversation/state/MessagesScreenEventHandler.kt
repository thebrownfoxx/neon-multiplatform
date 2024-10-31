package com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state

class MessagesScreenEventHandler(
    val onCall: () -> Unit,
    val onMarkAsRead: () -> Unit,
    val onMessageChange: (String) -> Unit,
    val onSend: () -> Unit,
    val onClose: () -> Unit,
) {
    companion object {
        val Blank = MessagesScreenEventHandler(
            onCall = {},
            onMarkAsRead = {},
            onMessageChange = {},
            onSend = {},
            onClose = {},
        )
    }
}