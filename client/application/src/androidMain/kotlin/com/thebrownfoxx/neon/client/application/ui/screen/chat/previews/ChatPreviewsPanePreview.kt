package com.thebrownfoxx.neon.client.application.ui.screen.chat.previews

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ChatPreviewsDummy
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ChatPreviewsEventHandler
import com.thebrownfoxx.neon.client.application.ui.theme.NeonTheme

@Preview
@Composable
private fun LoadingPreview() {
    NeonTheme {
        ChatPreviewsPane(
            state = ChatPreviewsDummy.LoadingChatPreviewsState,
            eventHandler = ChatPreviewsEventHandler.Blank,
        )
    }
}

@Preview
@Composable
private fun Preview() {
    NeonTheme {
        ChatPreviewsPane(
            state = ChatPreviewsDummy.ChatPreviewsState,
            eventHandler = ChatPreviewsEventHandler.Blank,
        )
    }
}