package com.thebrownfoxx.neon.client.application.ui.screen.chat.state

import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.ConversationPaneState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ChatPreviewsState

data class ChatScreenState(
    val chatPreviewsState: ChatPreviewsState,
    val conversationPaneState: ConversationPaneState?,
)