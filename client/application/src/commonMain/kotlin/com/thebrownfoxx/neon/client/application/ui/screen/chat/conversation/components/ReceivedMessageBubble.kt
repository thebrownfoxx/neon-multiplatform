package com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.GroupPosition

@Composable
fun ReceivedMessageBubble(
    content: String,
    read: Boolean,
    groupPosition: GroupPosition,
    modifier: Modifier = Modifier,
) {
    val roundedCorners = groupPosition.toReceivedMessageBubbleRoundedCorners()

    val containerColor by animateColorAsState(
        targetValue = when {
            read -> MaterialTheme.colorScheme.primaryContainer
            else -> MaterialTheme.colorScheme.tertiaryContainer
        },
        label = "containerColor",
    )

    val contentColor by animateColorAsState(
        targetValue = when {
            read -> MaterialTheme.colorScheme.onPrimaryContainer
            else -> MaterialTheme.colorScheme.onTertiaryContainer
        },
        label = "contentColor",
    )

    MessageBubble(
        content = content,
        roundedCorners = roundedCorners,
        containerColor = containerColor,
        contentColor = contentColor,
        modifier = modifier,
    )
}