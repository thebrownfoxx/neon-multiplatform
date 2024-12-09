package com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ChatPreviewsDummy
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ChatPreviewsEventHandler
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ChatPreviewsState
import com.thebrownfoxx.neon.client.application.ui.theme.NeonTheme
import com.thebrownfoxx.neon.common.type.Loaded
import com.thebrownfoxx.neon.common.type.Loading

@Preview
@Composable
private fun LoadingPreview() {
    NeonTheme {
        ChatPreviews(
            state = Loading,
            eventHandler = ChatPreviewsEventHandler.Blank,
        )
    }
}

@Preview
@Composable
private fun Preview() {
    NeonTheme {
        ChatPreviews(
            state = Loaded(
                ChatPreviewsState(
                    nudgedConversations = ChatPreviewsDummy.NudgedConversations,
                    unreadConversations = ChatPreviewsDummy.UnreadConversations,
                    readConversations = ChatPreviewsDummy.ReadConversations,
                ),
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
            state = Loaded(
                ChatPreviewsState(
                    nudgedConversations = emptyList(),
                    unreadConversations = ChatPreviewsDummy.UnreadConversations,
                    readConversations = ChatPreviewsDummy.ReadConversations,
                ),
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
            state = Loaded(
                ChatPreviewsState(
                    nudgedConversations = emptyList(),
                    unreadConversations = emptyList(),
                    readConversations = ChatPreviewsDummy.ReadConversations,
                ),
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
            state = Loaded(
                ChatPreviewsState(
                    nudgedConversations = emptyList(),
                    unreadConversations = ChatPreviewsDummy.UnreadConversations,
                    readConversations = emptyList(),
                ),
            ),
            eventHandler = ChatPreviewsEventHandler.Blank,
        )
    }
}