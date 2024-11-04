package com.thebrownfoxx.neon.client.application.ui.screen.chat.variant

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.thebrownfoxx.neon.client.application.ui.screen.chat.state.ChatScreenEventHandler
import com.thebrownfoxx.neon.client.application.ui.screen.chat.state.ChatScreenState
import com.thebrownfoxx.neon.client.application.ui.theme.NeonTheme
import com.thebrownfoxx.neon.common.type.Loading

@Preview(device = "spec:width=1280dp,height=800dp,dpi=240")
@Composable
private fun Preview() {
    NeonTheme {
        ExpandedChatScreen(
            state = ChatScreenState(
                chatPreviews = Loading,
                conversation = null,
            ),
            eventHandler = ChatScreenEventHandler.Blank,
        )
    }
}