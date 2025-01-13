package com.thebrownfoxx.neon.client.application.ui.screen.chat.variant

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.ConversationPaneState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.ConversationState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ChatPreviewsDummy
import com.thebrownfoxx.neon.client.application.ui.screen.chat.state.ChatScreenEventHandler
import com.thebrownfoxx.neon.client.application.ui.screen.chat.state.ChatScreenState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.state.ConversationDummy
import com.thebrownfoxx.neon.client.application.ui.theme.NeonTheme
import com.thebrownfoxx.neon.common.type.Loading

@Preview(device = "spec:width=411dp,height=891dp")
@Composable
private fun LoadingPreview() {
    NeonTheme {
        NonExpandedChatScreen(
            state = ChatScreenState(
                chatPreviewsState = ChatPreviewsDummy.LoadingChatPreviewsState,
                conversationPaneState = null,
            ),
            eventHandler = ChatScreenEventHandler.Blank,
        )
    }
}

@Preview(device = "spec:width=411dp,height=891dp")
@Composable
private fun LoadedPreview() {
    NeonTheme {
        NonExpandedChatScreen(
            state = ChatScreenState(
                chatPreviewsState = ChatPreviewsDummy.ChatPreviewsState,
                conversationPaneState = null,
            ),
            eventHandler = ChatScreenEventHandler.Blank,
        )
    }
}

@Preview(device = "spec:width=411dp,height=891dp")
@Composable
private fun SelectedLoadingPreview() {
    NeonTheme {
        NonExpandedChatScreen(
            state = ChatScreenState(
                chatPreviewsState = ChatPreviewsDummy.ChatPreviewsState,
                conversationPaneState = ConversationPaneState(
                    conversation = ConversationState(
                        info = Loading,
                        entries = emptyList(),
                        loading = true,
                    ),
                ),
            ),
            eventHandler = ChatScreenEventHandler.Blank,
        )
    }
}

@Preview(device = "spec:width=411dp,height=891dp")
@Composable
private fun SelectedPreview() {
    NeonTheme {
        NonExpandedChatScreen(
            state = ChatScreenState(
                chatPreviewsState = ChatPreviewsDummy.ChatPreviewsState,
                conversationPaneState = ConversationDummy.ConversationPaneState,
            ),
            eventHandler = ChatScreenEventHandler.Blank,
        )
    }
}