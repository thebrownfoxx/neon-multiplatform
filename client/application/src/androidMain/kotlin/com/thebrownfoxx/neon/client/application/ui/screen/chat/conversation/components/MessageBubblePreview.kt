package com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.thebrownfoxx.neon.client.application.ui.extension.RoundedCorners
import com.thebrownfoxx.neon.client.application.ui.theme.NeonTheme

@Preview
@Composable
private fun RoundedPreview() {
    NeonTheme {
        MessageBubble(
            content = "The quick brown foxx",
            roundedCorners = RoundedCorners.All,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview
@Composable
private fun NonRoundedPreview() {
    NeonTheme {
        MessageBubble(
            content = "The quick brown foxx",
            roundedCorners = RoundedCorners.None,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview
@Composable
private fun TextNotDrawnPreview() {
    NeonTheme {
        MessageBubble(
            content = "The quick brown foxx",
            roundedCorners = RoundedCorners.All,
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            drawText = false,
            modifier = Modifier.padding(16.dp),
        )
    }
}