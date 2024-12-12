package com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state

import com.thebrownfoxx.neon.common.type.id.GroupId

data class ChatPreviewsState(
    val nudgedConversations: List<ChatPreviewState>,
    val unreadConversations: List<ChatPreviewState>,
    val readConversations: List<ChatPreviewState>,
    val previewsToLoad: Set<GroupId> = emptySet(),
)
