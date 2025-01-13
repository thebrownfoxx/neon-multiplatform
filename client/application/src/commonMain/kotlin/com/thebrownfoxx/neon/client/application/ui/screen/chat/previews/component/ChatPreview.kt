package com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.twotone.Reply
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.thebrownfoxx.neon.client.application.ui.component.avatar.SmallAvatar
import com.thebrownfoxx.neon.client.application.ui.component.loader.AnimatedLoadableContent
import com.thebrownfoxx.neon.client.application.ui.extension.toReadableTime
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ChatPreviewContentState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ChatPreviewSenderState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ChatPreviewState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ChatPreviewStateValues
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ReceivedCommunityChatPreviewState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.SentChatPreviewState
import kotlinx.datetime.LocalDateTime
import neon.client.application.generated.resources.Res
import neon.client.application.generated.resources.deleted_group
import neon.client.application.generated.resources.from
import neon.client.application.generated.resources.start_a_conversation
import org.jetbrains.compose.resources.stringResource

@Composable
fun ChatPreview(
    state: ChatPreviewState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedLoadableContent(
        targetState = state.values,
        loader = { ChatPreviewLoader() },
        modifier = modifier,
    ) {
       LoadedChatPreview(
           values = it,
           onClick = onClick,
       )
    }
}

@Composable
private fun LoadedChatPreview(
    values: ChatPreviewStateValues,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    with(values) {
        Surface(modifier = modifier) {
            Surface(
                onClick = onClick,
                modifier = Modifier.padding(horizontal = 8.dp),
                shape = MaterialTheme.shapes.medium,
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    AvatarWithDelivery(
                        avatar = avatar,
                        delivery = content?.delivery,
                    )
                    PreviewTexts(
                        // TODO: This should ID if it is a deleted community or member
                        conversationName = name ?: stringResource(Res.string.deleted_group),
                        emphasized = emphasized,
                        content = content,
                    )
                }
            }
        }
    }
}

@Composable
private fun PreviewTexts(
    conversationName: String,
    emphasized: Boolean,
    content: ChatPreviewContentState?,
) {
    Column {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(modifier = Modifier.weight(1f)) {
                ConversationName(
                    name = conversationName,
                    emphasized = emphasized,
                )
            }
            if (content?.timestamp != null) Timestamp(content.timestamp)
        }
        PreviewContent(
            content = content,
            emphasized = emphasized,
        )
    }
}

@Composable
private fun ConversationName(
    name: String,
    emphasized: Boolean,
) {
    val defaultNameStyle = MaterialTheme.typography.titleMedium
    val nameStyle = when {
        emphasized -> defaultNameStyle.copy(fontWeight = FontWeight.Bold)
        else -> defaultNameStyle
    }

    Text(
        text = name,
        style = nameStyle,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun Timestamp(timestamp: LocalDateTime) {
    Text(
        text = timestamp.toReadableTime(),
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier.alpha(0.8f),
    )
}

@Composable
private fun PreviewContent(
    content: ChatPreviewContentState?,
    emphasized: Boolean,
) {
    val defaultTextStyle = MaterialTheme.typography.bodySmall

    val textStyle = when {
        emphasized -> defaultTextStyle.copy(fontWeight = FontWeight.SemiBold)
        else -> defaultTextStyle
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        // This is to make the preview the same size even if there is no icon shown,
        // without taking any space
        Spacer(modifier = Modifier.size(16.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = content?.message ?: stringResource(Res.string.start_a_conversation),
                style = textStyle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            SenderIcon(content?.sender)
        }
    }
}

@Composable
private fun SenderIcon(sender: ChatPreviewSenderState?) {
    if (sender is SentChatPreviewState) {
        Icon(
            imageVector = Icons.AutoMirrored.TwoTone.Reply,
            contentDescription = "From you: ",
            modifier = Modifier.size(16.dp),
        )
    } else if (sender is ReceivedCommunityChatPreviewState) {
        SmallAvatar(
            avatar = sender.senderAvatar,
            contentDescription = stringResource(
                Res.string.from,
                sender.senderAvatar.placeholder,
            ),
        )
    }
}