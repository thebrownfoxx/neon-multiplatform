package com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thebrownfoxx.neon.client.application.ui.component.loader.LargeAvatarLoader
import com.thebrownfoxx.neon.client.application.ui.component.loader.TextLoader

@Composable
fun ChatPreviewLoader(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        LargeAvatarLoader()
        Column {
            ConversationNameLoader()
            PreviewContentLoader()
        }
    }
}

@Composable
private fun ConversationNameLoader(modifier: Modifier = Modifier) {
    val placeholderText = "Lando Norris"

    TextLoader(
        text = placeholderText,
        style = MaterialTheme.typography.titleMedium,
        modifier = modifier,
    )
}

@Composable
private fun PreviewContentLoader(modifier: Modifier = Modifier) {
    val placeholderText = "it's friday theeeennnnnn! it's saturday..."

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier,
    ) {
        Spacer(modifier = Modifier.size(16.dp))
        TextLoader(
            text = placeholderText,
            style = MaterialTheme.typography.bodySmall,
            modifier = modifier
        )
    }
}