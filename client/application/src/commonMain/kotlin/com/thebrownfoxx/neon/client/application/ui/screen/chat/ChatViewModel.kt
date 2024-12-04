package com.thebrownfoxx.neon.client.application.ui.screen.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.ConversationPaneState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ChatPreviewState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ChatPreviewsDummy
import com.thebrownfoxx.neon.client.application.ui.screen.chat.state.ConversationDummy
import com.thebrownfoxx.neon.client.service.dependencies.model.GetGroupManagerError
import com.thebrownfoxx.neon.client.service.group.GroupManager
import com.thebrownfoxx.neon.common.outcome.Outcome
import com.thebrownfoxx.neon.common.type.Loaded
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    getGroupManager: suspend () -> Outcome<GroupManager, GetGroupManagerError>,
) : ViewModel() {
    private val groupManager: GroupManager? = null

    init {
        viewModelScope.launch {  }
    }

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