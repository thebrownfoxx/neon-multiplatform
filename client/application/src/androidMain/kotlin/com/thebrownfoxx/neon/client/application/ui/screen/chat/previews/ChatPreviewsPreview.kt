package com.thebrownfoxx.neon.client.application.ui.screen.chat.previews

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ChatPreviewsEventHandler
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.LoadedChatPreviewsState
import com.thebrownfoxx.neon.client.application.ui.theme.NeonTheme

@Preview
@Composable
private fun Preview() {
    NeonTheme {
        ChatPreviews(
            state = LoadedChatPreviewsState(
                nudgedConversations = ChatPreviewsDummy.NudgedConversations,
                unreadConversations = ChatPreviewsDummy.UnreadConversations,
                readConversations = ChatPreviewsDummy.ReadConversations,
            ),
            eventHandler = ChatPreviewsEventHandler.Blank,
        )
    }
}

@Preview
@Composable
private fun NoNudgedPreview() {
    NeonTheme {
        ChatPreviews(
            state = LoadedChatPreviewsState(
                nudgedConversations = emptyList(),
                unreadConversations = ChatPreviewsDummy.UnreadConversations,
                readConversations = ChatPreviewsDummy.ReadConversations,
            ),
            eventHandler = ChatPreviewsEventHandler.Blank,
        )
    }
}

@Preview
@Composable
private fun NoUnreadMessagesPreview() {
    NeonTheme {
        ChatPreviews(
            state = LoadedChatPreviewsState(
                nudgedConversations = emptyList(),
                unreadConversations = emptyList(),
                readConversations = ChatPreviewsDummy.ReadConversations,
            ),
            eventHandler = ChatPreviewsEventHandler.Blank,
        )
    }
}

@Preview
@Composable
private fun NoReadMessagesPreview() {
    NeonTheme {
        ChatPreviews(
            state = LoadedChatPreviewsState(
                nudgedConversations = emptyList(),
                unreadConversations = ChatPreviewsDummy.UnreadConversations,
                readConversations = emptyList(),
            ),
            eventHandler = ChatPreviewsEventHandler.Blank,
        )
    }
}