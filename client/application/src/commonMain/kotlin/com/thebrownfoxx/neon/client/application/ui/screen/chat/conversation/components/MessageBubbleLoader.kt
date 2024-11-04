package com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.thebrownfoxx.neon.client.application.ui.component.loader.Loader
import com.thebrownfoxx.neon.client.application.ui.extension.RoundedCorners
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.GroupPosition

@Composable
fun SentMessageBubbleLoader(
    text: String,
    groupPosition: GroupPosition,
    modifier: Modifier = Modifier,
) {
    MessageBubbleLoader(
        text = text,
        modifier = modifier,
        roundedCorners = groupPosition.toSentMessageBubbleRoundedCorners(),
    )
}

@Composable
fun ReceivedMessageBubbleLoader(
    text: String,
    groupPosition: GroupPosition,
    modifier: Modifier = Modifier,
) {
    MessageBubbleLoader(
        text = text,
        modifier = modifier,
        roundedCorners = groupPosition.toReceivedMessageBubbleRoundedCorners(),
    )
}

@Composable
private fun MessageBubbleLoader(
    text: String,
    modifier: Modifier = Modifier,
    roundedCorners: RoundedCorners = RoundedCorners.All,
) {
    Loader { color ->
        MessageBubble(
            content = text,
            roundedCorners = roundedCorners,
            containerColor = color,
            modifier = modifier,
            drawText = false,
        )
    }
}