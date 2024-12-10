package com.thebrownfoxx.neon.client.application.ui.screen.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thebrownfoxx.neon.client.application.ui.component.avatar.state.SingleAvatarState
import com.thebrownfoxx.neon.client.application.ui.component.delivery.state.DeliveryState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.ConversationPaneState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ChatPreviewContentState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ChatPreviewState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ChatPreviewsState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.SentState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.state.ConversationDummy
import com.thebrownfoxx.neon.client.model.LocalCommunity
import com.thebrownfoxx.neon.client.model.LocalMessage
import com.thebrownfoxx.neon.client.service.GroupManager
import com.thebrownfoxx.neon.client.service.MemberManager
import com.thebrownfoxx.neon.client.service.Messenger
import com.thebrownfoxx.neon.common.Logger
import com.thebrownfoxx.neon.common.extension.combineOrEmpty
import com.thebrownfoxx.neon.common.extension.toLocalDateTime
import com.thebrownfoxx.neon.common.type.Loaded
import com.thebrownfoxx.neon.common.type.Loading
import com.thebrownfoxx.outcome.ThrowingApi
import com.thebrownfoxx.outcome.map.getOrThrow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ChatViewModel(
    private val groupManager: GroupManager,
    private val memberManager: MemberManager,
    private val messenger: Messenger,
    private val logger: Logger,
) : ViewModel() {
    @OptIn(ThrowingApi::class)
    val chatPreviews = messenger.conversationPreviews.flatMapLatest { conversationPreviewsOutcome ->
        val conversationPreviews = conversationPreviewsOutcome.getOrThrow()

        val nudgedPreviews = conversationPreviews.nudgedPreviews.map { it.toChatPreviewState() }
        val unreadPreviews = conversationPreviews.unreadPreviews.map { it.toChatPreviewState() }
        val readPreviews = conversationPreviews.readPreviews.map { it.toChatPreviewState() }

        combine(
            combineOrEmpty(nudgedPreviews) { it },
            combineOrEmpty(unreadPreviews) { it },
            combineOrEmpty(readPreviews) { it },
        ) { nudged, unread, read ->
            Loaded(
                ChatPreviewsState(
                    nudgedConversations = nudged.toList(),
                    unreadConversations = unread.toList(),
                    readConversations = read.toList(),
                )
            ).also { logger.logDebug(it) }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, Loading)

    private val _conversation = MutableStateFlow<ConversationPaneState?>(null)
    val conversation = _conversation.asStateFlow()

    fun onConversationClick(chatPreviewState: ChatPreviewState) {
        val dummy = ConversationDummy.ConversationPaneState
        _conversation.value = dummy.copy(
            conversation = dummy.conversation.copy(
                info = dummy.conversation.info.map {
                    it.copy(groupId = chatPreviewState.groupId)
                }
            )
        )
    }

    fun onConversationClose() {
        _conversation.value = null
    }

    private suspend fun LocalMessage.toChatPreviewState(): Flow<ChatPreviewState> {
        return groupManager.getGroup(groupId).map { groupOutcome ->
            val group = groupOutcome.getOrThrow() as? LocalCommunity
            ChatPreviewState(
                groupId = groupId,
                avatar = group?.let {
                    SingleAvatarState(
                        url = it.avatarUrl,
                        placeholder = it.name,
                    )
                },
                name = group?.name ?: "TODO",
                content = ChatPreviewContentState(
                    message = content,
                    timestamp = timestamp.toLocalDateTime(),
                    delivery = DeliveryState.Delivered,
                    sender = SentState,
                ),
            )
        }
    }
}