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
        ReceivedMessageBubble(
            content = "What the sigma?",
            read = true,
            groupPosition = GroupPosition.Last,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview
@Composable
private fun TwoPreview() {
    NeonTheme {
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            ReceivedMessageBubble(
                content = "Skibidi skibidi",
                read = true,
                groupPosition = GroupPosition.Middle,
            )
            ReceivedMessageBubble(
                content = "What the sigma?",
                read = true,
                groupPosition = GroupPosition.Last,
            )
        }
    }
}

@Preview
@Composable
private fun ThreePreview() {
    NeonTheme {
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            ReceivedMessageBubble(
                content = "Skibidi skibidi",
                read = true,
                groupPosition = GroupPosition.Middle,
            )
            ReceivedMessageBubble(
                content = "What the sigma?",
                read = true,
                groupPosition = GroupPosition.Middle,
            )
            ReceivedMessageBubble(
                content = "WTF?",
                read = true,
                groupPosition = GroupPosition.Last,
            )
        }
    }
}

@Preview
@Composable
private fun LongPreview() {
    NeonTheme {
        ReceivedMessageBubble(
            content = LoremIpsum(50).values.first(),
            groupPosition = GroupPosition.Last,
            read = true,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview
@Composable
private fun MixedLengthPreview() {
    NeonTheme {
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            ReceivedMessageBubble(
                content = LoremIpsum(20).values.first(),
                read = true,
                groupPosition = GroupPosition.Middle,
            )
            ReceivedMessageBubble(
                content = "What the sigma?",
                read = true,
                groupPosition = GroupPosition.Middle,
            )
            ReceivedMessageBubble(
                content = LoremIpsum(10).values.first(),
                read = true,
                groupPosition = GroupPosition.Last,
            )
        }
    }
}



@Preview
@Composable
private fun UnreadPreview() {
    NeonTheme {
        ReceivedMessageBubble(
            content = "Skibidi skibidi",
            groupPosition = GroupPosition.Last,
            read = false,
            modifier = Modifier.padding(16.dp),
        )
    }
}