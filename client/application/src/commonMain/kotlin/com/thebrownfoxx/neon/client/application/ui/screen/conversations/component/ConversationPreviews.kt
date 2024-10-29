package com.thebrownfoxx.neon.client.application.ui.screen.conversations.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thebrownfoxx.neon.client.application.ui.extension.copy
import com.thebrownfoxx.neon.client.application.ui.extension.padding
import com.thebrownfoxx.neon.client.application.ui.screen.conversations.state.ConversationPreviewState
import neon.client.application.generated.resources.Res
import neon.client.application.generated.resources.conversations
import neon.client.application.generated.resources.nudged_conversations
import neon.client.application.generated.resources.read_conversations
import neon.client.application.generated.resources.unread_conversations
import org.jetbrains.compose.resources.stringResource

@Composable
fun ConversationPreviews(
    nudgedConversations: List<ConversationPreviewState>,
    unreadConversations: List<ConversationPreviewState>,
    readConversations: List<ConversationPreviewState>,
    onConversationClick: (ConversationPreviewState) -> Unit,
    onLoadMore: () -> Unit, // TODO: Implement this
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val headerPadding = 16.dp.padding.copy(bottom = 8.dp)

    Surface(modifier = modifier) {
        LazyColumn(contentPadding = contentPadding) {
            if (nudgedConversations.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(Res.string.nudged_conversations),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(headerPadding),
                    )
                }
            }
            items(
                items = nudgedConversations,
                key = { conversation -> conversation.groupId.value },
            ) { conversation ->
                ConversationPreview(
                    conversationPreview = conversation,
                    read = false,
                    onClick = { onConversationClick(conversation) },
                )
            }
            if (unreadConversations.isNotEmpty()) {
                item {
                    val label = stringResource(
                        when {
                            readConversations.isNotEmpty() -> Res.string.unread_conversations
                            else -> Res.string.conversations
                        },
                    )

                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(headerPadding),
                    )
                }
            }
            items(
                items = unreadConversations,
                key = { it.groupId.value },
            ) { conversation ->
                ConversationPreview(
                    conversationPreview = conversation,
                    read = false,
                    onClick = { onConversationClick(conversation) },
                )
            }
            if (readConversations.isNotEmpty()) {
                item {
                    val label = stringResource(
                        when {
                            unreadConversations.isNotEmpty() -> Res.string.read_conversations
                            else -> Res.string.conversations
                        },
                    )

                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(headerPadding),
                    )
                }
            }
            items(
                items = readConversations,
                key = { it.groupId.value },
            ) { conversation ->
                ConversationPreview(
                    conversationPreview = conversation,
                    read = true,
                    onClick = { onConversationClick(conversation) },
                )
            }
        }
    }
}