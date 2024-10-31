package com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.dp
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.GroupPosition
import com.thebrownfoxx.neon.client.application.ui.theme.NeonTheme

@Preview
@Composable
private fun SinglePreview() {
    NeonTheme {
        SentMessageBubble(
            content = "What the sigma?",
            groupPosition = GroupPosition.First,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview
@Composable
private fun TwoPreview() {
    NeonTheme {
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            SentMessageBubble(
                content = "Skibidi skibidi",
                groupPosition = GroupPosition.First,
            )
            SentMessageBubble(
                content = "What the sigma?",
                groupPosition = GroupPosition.Middle,
            )
        }
    }
}

@Preview
@Composable
private fun ThreePreview() {
    NeonTheme {
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            SentMessageBubble(
                content = "Skibidi skibidi",
                groupPosition = GroupPosition.First,
            )
            SentMessageBubble(
                content = "What the sigma?",
                groupPosition = GroupPosition.Middle,
            )
            SentMessageBubble(
                content = "WTF?",
                groupPosition = GroupPosition.Middle,
            )
        }
    }
}

@Preview
@Composable
private fun LongPreview() {
    NeonTheme {
        SentMessageBubble(
            content = LoremIpsum(50).values.first(),
            groupPosition = GroupPosition.First,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview
@Composable
private fun MixedLengthPreview() {
    NeonTheme {
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            SentMessageBubble(
                content = LoremIpsum(20).values.first(),
                groupPosition = GroupPosition.First,
            )
            SentMessageBubble(
                content = "What the sigma?",
                groupPosition = GroupPosition.Middle,
            )
            SentMessageBubble(
                content = LoremIpsum(10).values.first(),
                groupPosition = GroupPosition.Middle,
            )
        }
    }
}