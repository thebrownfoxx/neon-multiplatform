package com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.ConversationPaneEventHandler
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.ConversationPaneState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.ConversationState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.state.ConversationDummy
import com.thebrownfoxx.neon.client.application.ui.theme.NeonTheme
import com.thebrownfoxx.neon.common.type.Loading

@Preview
@Composable
private fun LoadingPreview() {
    NeonTheme {
        ConversationPane(
            state = ConversationPaneState(
                conversation = ConversationState(
                    info = Loading,
                    entries = emptyList(),
                    loading = true,
                )
            ),
            eventHandler = ConversationPaneEventHandler.Blank,
        )
    }
}

@Preview
@Composable
private fun Preview() {
    NeonTheme {
        ConversationPane(
            state = ConversationDummy.ConversationPaneState,
            eventHandler = ConversationPaneEventHandler.Blank,
        )
    }
}