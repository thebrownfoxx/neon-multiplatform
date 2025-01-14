package com.thebrownfoxx.neon.client.application.ui.screen.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.ConversationStateHandler
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.MessageListEntryId
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.ChatPreviewsStateHandler
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ChatPreviewStateId
import com.thebrownfoxx.neon.client.service.Authenticator
import com.thebrownfoxx.neon.client.service.GroupManager
import com.thebrownfoxx.neon.client.service.MemberManager
import com.thebrownfoxx.neon.client.service.Messenger
import com.thebrownfoxx.neon.common.Logger
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MessageId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    authenticator: Authenticator,
    groupManager: GroupManager,
    memberManager: MemberManager,
    private val messenger: Messenger,
    logger: Logger,
) : ViewModel() {
    private val chatPreviewIdMap = mutableMapOf<GroupId, ChatPreviewStateId>()

    private val lastVisibleGroupId = MutableStateFlow<GroupId?>(null)

    private val selectedConversationGroupId = MutableStateFlow<GroupId?>(null)

    private val chatPreviewsStateHandler = ChatPreviewsStateHandler(
        authenticator = authenticator,
        groupManager = groupManager,
        memberManager = memberManager,
        messenger = messenger,
        idMap = chatPreviewIdMap,
        lastVisibleGroupId = lastVisibleGroupId.asStateFlow(),
        externalScope = viewModelScope,
        logger = logger,
    )

    private val messageIdMap = mutableMapOf<MessageId, MessageListEntryId>()

    private val lastVisibleMessageId = MutableStateFlow<MessageId?>(null)

    private val message = MutableStateFlow("")

    private val conversationStateHandler = ConversationStateHandler(
        authenticator = authenticator,
        groupManager = groupManager,
        memberManager = memberManager,
        messenger = messenger,
        idMap = messageIdMap,
        selectedConversationGroupId = selectedConversationGroupId.asStateFlow(),
        lastVisibleMessageId = lastVisibleMessageId.asStateFlow(),
        message = message,
        externalScope = viewModelScope,
        logger = logger,
    )

    val chatPreviewsState = chatPreviewsStateHandler.previews

    val conversationPaneState = conversationStateHandler.paneState

    fun onConversationClick(groupId: GroupId) {
        selectedConversationGroupId.value = groupId
    }

    fun onLastVisiblePreviewChange(chatPreviewStateId: ChatPreviewStateId) {
        val groupId = chatPreviewStateId.toGroupId() ?: return
        lastVisibleGroupId.value = groupId
    }

    fun onConversationClose() {
        selectedConversationGroupId.value = null
    }

    fun onMessageChange(message: String) {
        this.message.value = message
    }

    fun onSendMessage() {
        val message = message.value
        this.message.value = ""
        viewModelScope.launch {
            val groupId = selectedConversationGroupId.value ?: return@launch
            messenger.sendMessage(groupId = groupId, content = message)
        }
    }

    fun onLastVisibleEntryChange(messageListEntryId: MessageListEntryId) {
        val messageId = messageListEntryId.toMessageId() ?: return
        lastVisibleMessageId.value = messageId
    }

    private fun ChatPreviewStateId.toGroupId(): GroupId? {
        return chatPreviewIdMap.filterValues { it == this }.keys.firstOrNull()
    }

    private fun MessageListEntryId.toMessageId(): MessageId? {
        return messageIdMap.filterValues { it == this }.keys.firstOrNull()
    }
}