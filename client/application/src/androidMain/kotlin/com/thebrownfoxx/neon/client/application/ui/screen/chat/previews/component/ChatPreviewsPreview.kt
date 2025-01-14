package com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ChatPreviewsDummy
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ChatPreviewsEventHandler
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ChatPreviewsState
import com.thebrownfoxx.neon.client.application.ui.theme.NeonTheme

@Preview
@Composable
private fun LoadingPreview() {
    NeonTheme {
        ChatPreviews(
            state = ChatPreviewsState(
                listItems = List(20) { loadingChatPreviewState() },
                ready = false,
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
            state = ChatPreviewsDummy.ChatPreviewsState,
            eventHandler = ChatPreviewsEventHandler.Blank,
        )
    }
}