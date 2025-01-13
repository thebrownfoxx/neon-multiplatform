package com.thebrownfoxx.neon.client.application.ui.screen.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.ConversationStateHandler
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.ChatPreviewsStateHandler
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ChatPreviewState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ChatPreviewStateId
import com.thebrownfoxx.neon.client.service.Authenticator
import com.thebrownfoxx.neon.client.service.GroupManager
import com.thebrownfoxx.neon.client.service.MemberManager
import com.thebrownfoxx.neon.client.service.Messenger
import com.thebrownfoxx.neon.common.Logger
import com.thebrownfoxx.neon.common.type.id.GroupId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModel(
    authenticator: Authenticator,
    groupManager: GroupManager,
    memberManager: MemberManager,
    private val messenger: Messenger,
    logger: Logger,
) : ViewModel() {
    private val idMap = mutableMapOf<GroupId, ChatPreviewStateId>()

    private val lastVisiblePreviewId = MutableStateFlow<ChatPreviewStateId?>(null)

    private val selectedChatPreviewId = MutableStateFlow<ChatPreviewStateId?>(null)

    private val selectedConversationGroupId = selectedChatPreviewId.mapLatest { it?.toGroupId() }

    private val chatPreviewsStateHandler = ChatPreviewsStateHandler(
        authenticator = authenticator,
        groupManager = groupManager,
        memberManager = memberManager,
        messenger = messenger,
        idMap = idMap,
        lastVisiblePreviewId = lastVisiblePreviewId.asStateFlow(),
        externalScope = viewModelScope,
        logger = logger,
    )

    private val message = MutableStateFlow("")

    private val conversationStateHandler = ConversationStateHandler(
        authenticator = authenticator,
        groupManager = groupManager,
        memberManager = memberManager,
        messenger = messenger,
        selectedConversationGroupId = selectedConversationGroupId,
        message = message,
        externalScope = viewModelScope,
        logger = logger,
    )

    val chatPreviewsState = chatPreviewsStateHandler.previews

    val conversationPaneState = conversationStateHandler.paneState

    fun onLastVisiblePreviewChange(chatPreviewStateId: ChatPreviewStateId) {
        lastVisiblePreviewId.value = chatPreviewStateId
    }

    fun onConversationClick(chatPreviewState: ChatPreviewState) {
        selectedChatPreviewId.value = chatPreviewState.id
    }

    fun onConversationClose() {
        selectedChatPreviewId.value = null
    }

    fun onMessageChange(message: String) {
        this.message.value = message
    }

    fun onSendMessage() {
        val message = message.value
        this.message.value = ""
        viewModelScope.launch {
            val groupId = selectedChatPreviewId.value?.toGroupId() ?: return@launch
            messenger.sendMessage(groupId = groupId, content = message)
        }
    }

    private fun ChatPreviewStateId.toGroupId(): GroupId {
        return idMap.filterValues { it == this }.keys.first()
    }
}