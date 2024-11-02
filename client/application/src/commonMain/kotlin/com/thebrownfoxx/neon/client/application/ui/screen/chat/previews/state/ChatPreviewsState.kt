package com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state

sealed interface ChatPreviewsState

data object LoadingChatPreviewsState : ChatPreviewsState

data class LoadedChatPreviewsState(
    val nudgedConversations: List<ChatPreviewState>,
    val unreadConversations: List<ChatPreviewState>,
    val readConversations: List<ChatPreviewState>,
) : ChatPreviewsState
