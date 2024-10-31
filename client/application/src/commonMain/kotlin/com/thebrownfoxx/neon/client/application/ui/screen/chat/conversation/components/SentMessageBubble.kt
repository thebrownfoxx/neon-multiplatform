package com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.thebrownfoxx.neon.client.application.ui.extension.Corner
import com.thebrownfoxx.neon.client.application.ui.extension.RoundedCorners
import com.thebrownfoxx.neon.client.application.ui.extension.Side
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.GroupPosition

@Composable
fun SentMessageBubble(
    content: String,
    groupPosition: GroupPosition,
    modifier: Modifier = Modifier,
) {
    val roundedCorners = when (groupPosition) {
        GroupPosition.Alone, GroupPosition.First ->
            RoundedCorners(Corner.TopStart, Corner.TopEnd, Corner.BottomStart)

        else -> RoundedCorners(Side.Start)
    }

    MessageBubble(
        content = content,
        roundedCorners = roundedCorners,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier,
    )
}