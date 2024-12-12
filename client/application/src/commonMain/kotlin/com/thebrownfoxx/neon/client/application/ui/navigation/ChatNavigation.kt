package com.thebrownfoxx.neon.client.application.ui.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.thebrownfoxx.neon.client.application.dependencies
import com.thebrownfoxx.neon.client.application.ui.screen.chat.ChatScreen
import com.thebrownfoxx.neon.client.application.ui.screen.chat.ChatViewModel
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.ConversationPaneEventHandler
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ChatPreviewsEventHandler
import com.thebrownfoxx.neon.client.application.ui.screen.chat.state.ChatScreenEventHandler
import com.thebrownfoxx.neon.client.application.ui.screen.chat.state.ChatScreenState
import kotlinx.serialization.Serializable

@Serializable
data object ChatRoute

fun NavGraphBuilder.chatDestination() = composable<ChatRoute> {
    val viewModel = viewModel {
        with(dependencies) {
            ChatViewModel(authenticator, groupManager, memberManager, messenger, logger)
        }
    }
    with(viewModel) {
        val chatPreviews by chatPreviews.collectAsStateWithLifecycle()
        val conversation by conversation.collectAsStateWithLifecycle()

        ChatScreen(
            state = ChatScreenState(
                chatPreviews = chatPreviews,
                conversation = conversation,
            ),
            eventHandler = ChatScreenEventHandler(
                chatPreviewsEventHandler = ChatPreviewsEventHandler(
                    onConversationClick = ::onConversationClick,
                    onLastVisiblePreviewChange = ::onLastVisiblePreviewChange,
                ),
                conversationPaneEventHandler = ConversationPaneEventHandler(
                    onCall = {},
                    onMessageChange = {},
                    onSend = {},
                    onMarkAsRead = {},
                    onClose = ::onConversationClose,
                )
            ),
        )
    }
}

fun NavController.navigateToChat() = navigate(ChatRoute) {
    popUpTo(graph.startDestinationId) { inclusive = true }
    launchSingleTop = true
}