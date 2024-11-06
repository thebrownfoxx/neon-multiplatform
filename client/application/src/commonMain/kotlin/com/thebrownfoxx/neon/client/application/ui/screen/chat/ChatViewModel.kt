package com.thebrownfoxx.neon.client.application.ui.screen.chat

import androidx.lifecycle.ViewModel
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.ConversationPaneState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ChatPreviewState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ChatPreviewsDummy
import com.thebrownfoxx.neon.client.application.ui.screen.chat.state.ConversationDummy
import com.thebrownfoxx.neon.common.type.Loaded
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ChatViewModel : ViewModel() {
    private val _chatPreviews = MutableStateFlow(Loaded(ChatPreviewsDummy.ChatPreviewsState))
    val chatPreviews = _chatPreviews.asStateFlow()

    private val _conversation = MutableStateFlow<ConversationPaneState?>(null)
    val conversation = _conversation.asStateFlow()

    fun onConversationClick(chatPreviewState: ChatPreviewState) {
        _conversation.value = ConversationDummy.ConversationPaneState
    }

    fun onConversationClose() {
        _conversation.value = null
    }
}