package com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.GroupPosition

@Composable
fun SentMessageBubble(
    content: String,
    groupPosition: GroupPosition,
    modifier: Modifier = Modifier,
) {
    MessageBubble(
        content = content,
        roundedCorners = groupPosition.toSentMessageBubbleRoundedCorners(),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier,
    )
}