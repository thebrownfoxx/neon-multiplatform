package com.thebrownfoxx.neon.client.application.ui.screen.chat.previews

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ChatPreviewsEventHandler
import com.thebrownfoxx.neon.client.application.ui.theme.NeonTheme
import com.thebrownfoxx.neon.common.type.Loaded
import com.thebrownfoxx.neon.common.type.Loading

@Preview
@Composable
private fun LoadingPreview() {
    NeonTheme {
        ChatPreviewsPane(
            state = Loading,
            eventHandler = ChatPreviewsEventHandler.Blank,
        )
    }
}

@Preview
@Composable
private fun Preview() {
    NeonTheme {
        ChatPreviewsPane(
            state = Loaded(ChatPreviewsDummy.ChatPreviewsState),
            eventHandler = ChatPreviewsEventHandler.Blank,
        )
    }
}