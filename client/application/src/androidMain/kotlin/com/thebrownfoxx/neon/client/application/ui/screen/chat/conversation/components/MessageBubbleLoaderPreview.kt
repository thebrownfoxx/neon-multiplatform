package com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.components

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.GroupPosition
import com.thebrownfoxx.neon.client.application.ui.theme.NeonTheme

@Preview
@Composable
private fun SentPreview() {
    NeonTheme {
        SentMessageBubbleLoader(
            text = "The quick brown fox jumps over the lazy dog",
            groupPosition = GroupPosition.Alone,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview
@Composable
private fun ReceivedPreview() {
    NeonTheme {
        ReceivedMessageBubbleLoader(
            text = "Lala lala. Lala lala.",
            groupPosition = GroupPosition.Alone,
            modifier = Modifier.padding(16.dp),
        )
    }
}