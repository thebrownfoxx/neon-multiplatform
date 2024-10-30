package com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state

sealed interface ChatScreenState

data class LoadingChatScreenState(
    val nudgedConversationsCount: Int,
    val unreadConversationsCount: Int,
    val readConversationsCount: Int,
) : ChatScreenState

data class LoadedChatScreenState(
    val nudgedConversations: List<ChatPreviewState>,
    val unreadConversations: List<ChatPreviewState>,
    val readConversations: List<ChatPreviewState>,
) : ChatScreenState
