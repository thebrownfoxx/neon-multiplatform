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

@Deprecated("Use individual ChatPreview instead")
@Composable
fun ChatPreviewsLoader(
    nudgedConversationsCount: Int,
    unreadConversationsCount: Int,
    readConversationsCount: Int,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    LazyColumn(
        contentPadding = contentPadding,
        modifier = modifier,
        userScrollEnabled = false,
    ) {
        item { HeaderLoader(text = stringResource(Res.string.nudged_conversations)) }
        items(count = nudgedConversationsCount) { ChatPreviewLoader() }
        item { HeaderLoader(text = stringResource(Res.string.unread_conversations)) }
        items(count = unreadConversationsCount) { ChatPreviewLoader() }
        item { HeaderLoader(text = stringResource(Res.string.read_conversations)) }
        items(count = readConversationsCount) { ChatPreviewLoader() }
    }
}

@Composable
private fun HeaderLoader(text: String) {
    TextLoader(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(16.dp.padding.copy(bottom = 8.dp)),
    )
}