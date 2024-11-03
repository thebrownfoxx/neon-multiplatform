package com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thebrownfoxx.neon.client.application.ui.component.loader.TextLoader
import com.thebrownfoxx.neon.client.application.ui.extension.copy
import com.thebrownfoxx.neon.client.application.ui.extension.padding
import neon.client.application.generated.resources.Res
import neon.client.application.generated.resources.nudged_conversations
import neon.client.application.generated.resources.read_conversations
import neon.client.application.generated.resources.unread_conversations
import org.jetbrains.compose.resources.stringResource

@Composable
fun ChatPreviewsLoader(
    nudgedConversationsCount: Int,
    unreadConversationsCount: Int,
    readConversationsCount: Int,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val headerPadding = 16.dp.padding.copy(bottom = 8.dp)

    LazyColumn(
        contentPadding = contentPadding,
        modifier = modifier,
        userScrollEnabled = false,
    ) {
        item {
            TextLoader(
                text = stringResource(Res.string.nudged_conversations),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(headerPadding),
            )
        }
        items(count = nudgedConversationsCount) {
            ChatPreviewLoader()
        }
        item {
            TextLoader(
                text = stringResource(Res.string.unread_conversations),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(headerPadding),
            )
        }
        items(count = unreadConversationsCount) {
            ChatPreviewLoader()
        }
        item {
            TextLoader(
                text = stringResource(Res.string.read_conversations),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(headerPadding),
            )
        }
        items(count = readConversationsCount) {
            ChatPreviewLoader()
        }
    }
}