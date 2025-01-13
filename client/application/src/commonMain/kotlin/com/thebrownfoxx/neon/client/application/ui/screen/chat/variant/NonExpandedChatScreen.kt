package com.thebrownfoxx.neon.client.application.ui.screen.chat.variant

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.thebrownfoxx.neon.client.application.ui.extension.SafeDrawingPadding
import com.thebrownfoxx.neon.client.application.ui.extension.sharedZAxisEnter
import com.thebrownfoxx.neon.client.application.ui.extension.sharedZAxisExit
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.ConversationPane
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.ChatPreviewsPane
import com.thebrownfoxx.neon.client.application.ui.screen.chat.state.ChatScreenEventHandler
import com.thebrownfoxx.neon.client.application.ui.screen.chat.state.ChatScreenState

@Composable
fun NonExpandedChatScreen(
    state: ChatScreenState,
    eventHandler: ChatScreenEventHandler,
    modifier: Modifier = Modifier,
) {
    with(state) {
        with(eventHandler) {
            AnimatedContent(
                targetState = conversationPaneState,
                transitionSpec = {
                    val reversed = targetState == null
                    sharedZAxisEnter(reversed) togetherWith sharedZAxisExit(reversed)
                },
                contentKey = { it?.conversation?.groupId },
                modifier = Modifier.consumeWindowInsets(WindowInsets.safeDrawing)
            ) {
                when (it) {
                    null -> ChatPreviewsPane(
                        state = chatPreviewsState,
                        eventHandler = chatPreviewsEventHandler,
                        modifier = modifier,
                        contentPadding = SafeDrawingPadding,
                    )
                    else -> ConversationPane(
                        state = it,
                        eventHandler = conversationPaneEventHandler,
                        modifier = modifier,
                        contentPadding = SafeDrawingPadding,
                    )
                }
            }
        }
    }
}