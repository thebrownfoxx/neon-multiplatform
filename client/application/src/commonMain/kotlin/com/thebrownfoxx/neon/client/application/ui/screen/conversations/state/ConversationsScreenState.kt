package com.thebrownfoxx.neon.client.application.ui.screen.conversations.state

sealed interface ConversationsScreenState

data class LoadingConversationsScreenState(
    val nudgedConversationsCount: Int,
    val unreadConversationsCount: Int,
    val readConversationsCount: Int,
) : ConversationsScreenState

data class LoadedConversationsScreenState(
    val nudgedConversations: List<ConversationPreviewState>,
    val unreadConversations: List<ConversationPreviewState>,
    val readConversations: List<ConversationPreviewState>,
) : ConversationsScreenState
