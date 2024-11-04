package com.thebrownfoxx.neon.client.application.ui.screen.chat.state

import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.ConversationPaneState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ChatPreviewsState
import com.thebrownfoxx.neon.common.type.Loadable

data class ChatScreenState(
    val chatPreviews: Loadable<ChatPreviewsState>,
    val conversation: ConversationPaneState?,
)