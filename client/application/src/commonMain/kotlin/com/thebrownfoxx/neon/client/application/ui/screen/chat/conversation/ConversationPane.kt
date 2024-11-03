package com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.ConversationPaneEventHandler
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.ConversationPaneState

@Composable
fun ConversationPane(
    state: ConversationPaneState,
    eventHandler: ConversationPaneEventHandler,
    modifier: Modifier = Modifier,
) {
    with(state) {
        with(eventHandler) {

        }
    }
}