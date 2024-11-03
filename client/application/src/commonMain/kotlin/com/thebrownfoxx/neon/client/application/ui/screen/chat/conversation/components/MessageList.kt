package com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Visibility
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.thebrownfoxx.neon.client.application.ui.component.common.AnimatedVisibility
import com.thebrownfoxx.neon.client.application.ui.component.common.Button
import com.thebrownfoxx.neon.client.application.ui.component.common.ButtonIconText
import com.thebrownfoxx.neon.client.application.ui.component.common.ExpandAxis
import com.thebrownfoxx.neon.client.application.ui.component.delivery.state.DeliveryState
import com.thebrownfoxx.neon.client.application.ui.extension.toReadableTime
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.ChunkTimestamp
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.MessageEntry
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.MessageListEntry
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.ReceivedCommunityState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.ReceivedDirectState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.SentState
import kotlinx.datetime.LocalDateTime
import neon.client.application.generated.resources.Res
import neon.client.application.generated.resources.mark_as_read
import org.jetbrains.compose.resources.stringResource

@Composable
fun MessageList(
    entries: List<MessageListEntry>,
    onMarkAsRead: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val lastEntry = remember(entries) { entries.lastOrNull() }

    val read = remember(lastEntry) {
        lastEntry is MessageEntry && lastEntry.message.deliveryState == DeliveryState.Read
    }

    LazyColumn(
        modifier = modifier,
        reverseLayout = true,
        contentPadding = contentPadding,
    ) {
        item {
            MarkAsReadButton(
                visible = !read,
                onClick = onMarkAsRead,
            )
        }

        entries(entries)
    }
}

private fun LazyListScope.entries(entries: List<MessageListEntry>) {
    itemsIndexed(
        items = entries,
        key = { _, item -> item.id.value },
    ) { index, entry ->
        val spacing = when {
            entry.mustSpace -> 16.dp
            index == 0 -> 0.dp
            else -> 4.dp
        }

        Column {
            when (entry) {
                is ChunkTimestamp -> ChunkTimestampLabel(timestamp = entry.timestamp)
                is MessageEntry -> when (val sender = entry.message.sender) {
                    SentState -> SentMessageListBubble(
                        content = entry.message.content,
                        groupPosition = entry.message.groupPosition,
                    )
                    ReceivedDirectState -> ReceivedDirectMessageListBubble(
                        content = entry.message.content,
                        groupPosition = entry.message.groupPosition,
                        read = entry.message.deliveryState == DeliveryState.Read,
                    )
                    is ReceivedCommunityState -> ReceivedCommunityMessageListBubble(
                        senderAvatar = sender.senderAvatar,
                        content = entry.message.content,
                        groupPosition = entry.message.groupPosition,
                        read = entry.message.deliveryState == DeliveryState.Read,
                    )
                }
            }
            Spacer(modifier = Modifier.height(spacing))
        }
    }
}

@Composable
private fun MarkAsReadButton(
    visible: Boolean,
    onClick: () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        expandAxis = ExpandAxis.Vertical,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
        ) {
            Button(
                onClick = onClick,
                modifier = Modifier.align(Alignment.Center),
            ) {
                ButtonIconText(
                    icon = Icons.TwoTone.Visibility,
                    iconContentDescription = null,
                    text = stringResource(Res.string.mark_as_read),
                )
            }
        }
    }
}

@Composable
private fun ChunkTimestampLabel(timestamp: LocalDateTime) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = timestamp.toReadableTime(),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier
                .align(Alignment.Center)
                .alpha(0.8f),
        )
    }
}
