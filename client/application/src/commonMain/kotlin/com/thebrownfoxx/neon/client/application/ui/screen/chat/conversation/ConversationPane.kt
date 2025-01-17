package com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thebrownfoxx.neon.client.application.ui.extension.PaddingSide
import com.thebrownfoxx.neon.client.application.ui.extension.except
import com.thebrownfoxx.neon.client.application.ui.extension.horizontal
import com.thebrownfoxx.neon.client.application.ui.extension.minus
import com.thebrownfoxx.neon.client.application.ui.extension.plus
import com.thebrownfoxx.neon.client.application.ui.extension.verticalPadding
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.components.ConversationTitleBar
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.components.MessageFieldBar
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.components.MessageList
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.ConversationPaneEventHandler
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.ConversationPaneState
import com.thebrownfoxx.neon.common.type.Loaded

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
                        contentPadding = contentPadding.except(PaddingSide.Bottom),
                    )
                },
                bottomBar = {
                    MessageFieldBar(
                        message = message,
                        onMessageChange = onMessageChange,
                        onSend = onSend,
                        contentPadding = contentPadding.except(PaddingSide.Top),
                    )
                },
                modifier = modifier,
            ) { innerPadding ->
                val isCommunity = conversation.info is Loaded && conversation.info.value.isCommunity
                MessageList(
                    entries = conversation.entries,
                    isCommunity = isCommunity,
                    loading = conversation.loadingEntries,
                    onMarkAsRead = onMarkAsRead,
                    onLastVisibleEntryChange = onLastVisibleEntryChange,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = innerPadding + contentPadding.horizontal -
                            16.dp.verticalPadding,
                )
            }
        }
    }
}