package com.thebrownfoxx.neon.client.application.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.thebrownfoxx.neon.client.application.ui.screen.chat.ChatScreen
import com.thebrownfoxx.neon.client.application.ui.screen.chat.state.ChatScreenEventHandler
import com.thebrownfoxx.neon.client.application.ui.screen.chat.state.ChatScreenState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.state.ConversationDummy
import com.thebrownfoxx.neon.client.application.ui.screen.login.state.ChatPreviewsDummy
import com.thebrownfoxx.neon.common.type.Loaded
import kotlinx.serialization.Serializable

@Serializable
data object ChatNavigation

fun NavGraphBuilder.chatDestination() {
    composable<ChatNavigation> {
        ChatScreen(
            state = ChatScreenState(
                chatPreviews = Loaded(ChatPreviewsDummy.ChatPreviewsState),
                conversation = ConversationDummy.ConversationPaneState,
            ),
            eventHandler = ChatScreenEventHandler.Blank,
        )
    }
}