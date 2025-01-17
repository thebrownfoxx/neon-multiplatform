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
        val chatPreviews by chatPreviewsState.collectAsStateWithLifecycle()
        val conversation by conversationPaneState.collectAsStateWithLifecycle()

        val state = ChatScreenState(
            chatPreviewsState = chatPreviews,
            conversationPaneState = conversation,
        )
        val chatPreviewsEventHandler = ChatPreviewsEventHandler(
            onConversationClick = ::onConversationClick,
            onLastVisiblePreviewChange = ::onLastVisiblePreviewChange,
        )
        val conversationPaneEventHandler = ConversationPaneEventHandler(
            onCall = {},
            onMessageChange = ::onMessageChange,
            onSend = ::onSendMessage,
            onMarkAsRead = {},
            onClose = ::onConversationClose,
            onLastVisibleEntryChange = ::onLastVisibleEntryChange,
        )
        val eventHandler = ChatScreenEventHandler(
            chatPreviewsEventHandler = chatPreviewsEventHandler,
            conversationPaneEventHandler = conversationPaneEventHandler,
        )
        ChatScreen(
            state = state,
            eventHandler = eventHandler,
        )
    }
}

fun NavController.navigateToChat() = navigate(ChatRoute) {
    popUpTo(graph.startDestinationId) { inclusive = true }
    launchSingleTop = true
}