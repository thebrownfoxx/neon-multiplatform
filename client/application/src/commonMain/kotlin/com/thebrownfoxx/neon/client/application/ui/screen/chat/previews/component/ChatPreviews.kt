package com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thebrownfoxx.neon.client.application.ui.component.loader.AnimatedLoadableContent
import com.thebrownfoxx.neon.client.application.ui.extension.copy
import com.thebrownfoxx.neon.client.application.ui.extension.padding
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ChatPreviewState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ChatPreviewsEventHandler
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ChatPreviewsState
import com.thebrownfoxx.neon.common.type.Loadable
import neon.client.application.generated.resources.Res
import neon.client.application.generated.resources.conversations
import neon.client.application.generated.resources.nudged_conversations
import neon.client.application.generated.resources.read_conversations
import neon.client.application.generated.resources.unread_conversations
import org.jetbrains.compose.resources.stringResource

@Composable
fun ChatPreviews(
    state: Loadable<ChatPreviewsState>,
    eventHandler: ChatPreviewsEventHandler,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    Surface(modifier = modifier) {
        AnimatedLoadableContent(
            targetState = state,
            loader = { LoadingChatPreviews(contentPadding = contentPadding) },
        ) {
            LoadedChatPreviews(
                state = it,
                eventHandler = eventHandler,
                contentPadding = contentPadding,
            )
        }
    }
}

@Composable
private fun LoadingChatPreviews(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    ChatPreviewsLoader(
        nudgedConversationsCount = 2,
        unreadConversationsCount = 4,
        readConversationsCount = 10,
        modifier = modifier,
        contentPadding = contentPadding,
    )
}

@Composable
private fun LoadedChatPreviews(
    state: ChatPreviewsState,
    eventHandler: ChatPreviewsEventHandler,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    with(state) {
        with(eventHandler) {
            LazyColumn(
                contentPadding = contentPadding,
                modifier = modifier,
            ) {
                nudgedConversations(
                    nudgedConversations = nudgedConversations,
                    onConversationClick = onConversationClick,
                )
                unreadConversations(
                    unreadConversations = unreadConversations,
                    readConversationsEmpty = readConversations.isEmpty(),
                    onConversationClick = onConversationClick,
                )
                readConversations(
                    readConversations = readConversations,
                    unreadConversationsEmpty = unreadConversations.isEmpty(),
                    onConversationClick = onConversationClick,
                )
            }
        }
    }
}

private fun LazyListScope.nudgedConversations(
    nudgedConversations: List<ChatPreviewState>,
    onConversationClick: (ChatPreviewState) -> Unit,
) {
    if (nudgedConversations.isNotEmpty()) nudgedHeader()
    items(
        items = nudgedConversations,
        key = { conversation -> conversation.groupId.value },
    ) { conversation ->
        ChatPreview(
            state = conversation,
            onClick = { onConversationClick(conversation) },
        )
    }
}

private fun LazyListScope.nudgedHeader() {
    item { Header(text = stringResource(Res.string.nudged_conversations)) }
}

private fun LazyListScope.unreadConversations(
    unreadConversations: List<ChatPreviewState>,
    readConversationsEmpty: Boolean,
    onConversationClick: (ChatPreviewState) -> Unit,
) {
    if (unreadConversations.isNotEmpty()) unreadHeader(readConversationsEmpty)
    items(
        items = unreadConversations,
        key = { it.groupId.value },
    ) { conversation ->
        ChatPreview(
            state = conversation,
            onClick = { onConversationClick(conversation) },
        )
    }
}

private fun LazyListScope.unreadHeader(readConversationsEmpty: Boolean) {
    item {
        val label = stringResource(
            when {
                readConversationsEmpty -> Res.string.conversations
                else -> Res.string.unread_conversations
            },
        )

        Header(label)
    }
}

private fun LazyListScope.readConversations(
    readConversations: List<ChatPreviewState>,
    unreadConversationsEmpty: Boolean,
    onConversationClick: (ChatPreviewState) -> Unit,
) {
    if (readConversations.isNotEmpty()) readHeader(unreadConversationsEmpty)
    items(
        items = readConversations,
        key = { it.groupId.value },
    ) { conversation ->
        ChatPreview(
            state = conversation,
            onClick = { onConversationClick(conversation) },
        )
    }
}

private fun LazyListScope.readHeader(unreadConversationsEmpty: Boolean) {
    item {
        val label = stringResource(
            when {
                unreadConversationsEmpty -> Res.string.conversations
                else -> Res.string.read_conversations
            },
        )

        Header(label)
    }
}

@Composable
private fun Header(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(16.dp.padding.copy(bottom = 8.dp)),
    )
}