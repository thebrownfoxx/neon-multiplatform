package com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thebrownfoxx.neon.client.application.ui.extension.bottom
import com.thebrownfoxx.neon.client.application.ui.extension.horizontalPadding
import com.thebrownfoxx.neon.client.application.ui.extension.plus
import com.thebrownfoxx.neon.client.application.ui.extension.top
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.components.ConversationTitleBar
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.components.MessageFieldBar
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.components.MessageList
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.ConversationPaneEventHandler
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.ConversationPaneState

@Composable
fun ConversationPane(
    state: ConversationPaneState,
    eventHandler: ConversationPaneEventHandler,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    with(state) {
        with(eventHandler) {
            Scaffold(
                topBar = {
                    ConversationTitleBar(
                        info = conversation.info,
                        onCall = onCall,
                        onClose = onClose,
                        contentPadding = contentPadding.top,
                    )
                },
                bottomBar = {
                    MessageFieldBar(
                        message = message,
                        onMessageChange = onMessageChange,
                        onSend = onSend,
                        contentPadding = contentPadding.bottom,
                    )
                },
                modifier = modifier,
            ) { innerPadding ->
                MessageList(
                    entries = conversation.entries,
                    onMarkAsRead = onMarkAsRead,
                    contentPadding = innerPadding + 16.dp.horizontalPadding,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}