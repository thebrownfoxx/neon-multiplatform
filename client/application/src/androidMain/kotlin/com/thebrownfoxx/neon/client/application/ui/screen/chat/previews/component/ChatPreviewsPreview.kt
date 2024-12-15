package com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ChatPreviewsDummy
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ChatPreviewsEventHandler
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ChatPreviewsState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.LoadingChatPreviewState
import com.thebrownfoxx.neon.client.application.ui.theme.NeonTheme

@Preview
@Composable
private fun LoadingPreview() {
    NeonTheme {
        ChatPreviews(
            state = ChatPreviewsState(
                nudgedConversations = List(2) { LoadingChatPreviewState() },
                unreadConversations = List(10) { LoadingChatPreviewState() },
                readConversations = List(30) { LoadingChatPreviewState() },
            ),
            eventHandler = ChatPreviewsEventHandler.Blank,
        )
    }
}

@Preview
@Composable
private fun Preview() {
    NeonTheme {
        ChatPreviews(
            state = ChatPreviewsState(
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
            state = ChatPreviewsState(
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
            state = ChatPreviewsState(
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
            state = ChatPreviewsState(
                nudgedConversations = emptyList(),
                unreadConversations = ChatPreviewsDummy.UnreadConversations,
                readConversations = emptyList(),
            ),
            eventHandler = ChatPreviewsEventHandler.Blank,
        )
    }
}