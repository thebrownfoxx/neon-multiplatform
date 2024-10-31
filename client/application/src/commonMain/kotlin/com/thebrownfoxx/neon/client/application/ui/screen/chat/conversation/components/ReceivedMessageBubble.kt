package com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.thebrownfoxx.neon.client.application.ui.extension.Corner
import com.thebrownfoxx.neon.client.application.ui.extension.RoundedCorners
import com.thebrownfoxx.neon.client.application.ui.extension.Side
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.GroupPosition

@Composable
fun ReceivedMessageBubble(
    content: String,
    read: Boolean,
    groupPosition: GroupPosition,
    modifier: Modifier = Modifier,
) {
    val roundedCorners = when (groupPosition) {
        GroupPosition.Alone, GroupPosition.Last ->
            RoundedCorners(Corner.TopEnd, Corner.BottomEnd, Corner.BottomStart)

        else -> RoundedCorners(Side.End)
    }

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