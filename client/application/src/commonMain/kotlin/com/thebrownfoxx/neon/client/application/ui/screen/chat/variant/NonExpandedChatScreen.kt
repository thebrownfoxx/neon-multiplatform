package com.thebrownfoxx.neon.client.application.ui.screen.chat.variant

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.thebrownfoxx.neon.client.application.ui.extension.sharedZAxisEnter
import com.thebrownfoxx.neon.client.application.ui.extension.sharedZAxisExit
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.ConversationPane
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.ChatPreviewsPane
import com.thebrownfoxx.neon.client.application.ui.screen.chat.state.ChatScreenEventHandler
import com.thebrownfoxx.neon.client.application.ui.screen.chat.state.ChatScreenState
import com.thebrownfoxx.neon.common.type.getOrNull

@Composable
fun NonExpandedChatScreen(
    state: ChatScreenState,
    eventHandler: ChatScreenEventHandler,
    modifier: Modifier = Modifier,
) {
    with(state) {
        with(eventHandler) {
            AnimatedContent(
                targetState = conversation,
                transitionSpec = {
                    val reversed = targetState != null
                    sharedZAxisEnter(reversed) togetherWith sharedZAxisExit(reversed)
                },
                contentKey = { it?.conversation?.info?.getOrNull()?.groupId },
            ) {
                when (it) {
                    null -> ChatPreviewsPane(
                        state = chatPreviews,
                        eventHandler = chatPreviewsEventHandler,
                        modifier = modifier,
                    )
                    else -> ConversationPane(
                        state = it,
                        eventHandler = conversationPaneEventHandler,
                        modifier = modifier,
                    )
                }
            }
        }
    }
}