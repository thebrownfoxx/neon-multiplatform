package com.thebrownfoxx.neon.client.application.ui.screen.chat.variant

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.twotone.Message
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.thebrownfoxx.neon.client.application.ui.component.Spacer
import com.thebrownfoxx.neon.client.application.ui.component.twopane.Pane
import com.thebrownfoxx.neon.client.application.ui.component.twopane.TwoPaneLayout
import com.thebrownfoxx.neon.client.application.ui.extension.RoundedCorners
import com.thebrownfoxx.neon.client.application.ui.extension.Side
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.ConversationPane
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.ConversationPaneEventHandler
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.ConversationPaneState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.ChatPreviewsPane
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ChatPreviewsEventHandler
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ChatPreviewsState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.state.ChatScreenEventHandler
import com.thebrownfoxx.neon.client.application.ui.screen.chat.state.ChatScreenState
import com.thebrownfoxx.neon.common.type.Loadable
import neon.client.application.generated.resources.Res
import neon.client.application.generated.resources.no_conversation_selected
import org.jetbrains.compose.resources.stringResource

@Composable
fun ExpandedChatScreen(
    state: ChatScreenState,
    eventHandler: ChatScreenEventHandler,
    modifier: Modifier = Modifier,
) {
    with(state) {
        with(eventHandler) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = modifier,
            ) {
                TwoPaneLayout(
                    leftPane = {
                        LeftPane(
                            chatPreviews = chatPreviews,
                            chatPreviewsEventHandler = chatPreviewsEventHandler,
                        )
                    },
                    rightPane = {
                        RightPane(
                            conversation = conversation,
                            conversationPaneEventHandler = conversationPaneEventHandler,
                        )
                    },
                    modifier = Modifier.padding(24.dp),
                    center = 0.3f,
                )
            }
        }
    }
}

@Composable
private fun LeftPane(
    chatPreviews: Loadable<ChatPreviewsState>,
    chatPreviewsEventHandler: ChatPreviewsEventHandler,
) {
    Pane(roundedCorners = RoundedCorners(Side.Start)) {
        ChatPreviewsPane(
            state = chatPreviews,
            eventHandler = chatPreviewsEventHandler,
        )
    }
}

@Composable
private fun RightPane(
    conversation: ConversationPaneState?,
    conversationPaneEventHandler: ConversationPaneEventHandler,
) {
    Pane(roundedCorners = RoundedCorners(Side.End)) {
        when (conversation) {
            null -> RightPanePlaceholder()
            else -> ConversationPane(
                state = conversation,
                eventHandler = conversationPaneEventHandler,
            )
        }
    }
}

@Composable
private fun RightPanePlaceholder() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .alpha(0.8f)
            .fillMaxSize(),
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.TwoTone.Message,
            contentDescription = null,
        )
        Spacer(height = 8.dp)
        Text(
            text = stringResource(Res.string.no_conversation_selected),
            style = MaterialTheme.typography.titleLarge,
        )
    }
}