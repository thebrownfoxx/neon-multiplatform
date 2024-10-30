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
import com.thebrownfoxx.neon.client.application.ui.extension.toReadableTime
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ChatPreviewState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ChatPreviewContentState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ReceivedCommunityState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.SenderState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.SentState
import neon.client.application.generated.resources.Res
import neon.client.application.generated.resources.deleted_group
import neon.client.application.generated.resources.from
import neon.client.application.generated.resources.start_a_conversation
import org.jetbrains.compose.resources.stringResource

@Composable
fun ChatPreview(
    chatPreview: ChatPreviewState,
    read: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // TODO: This should ID if it is a deleted community or member
    val conversationName = chatPreview.name ?: stringResource(Res.string.deleted_group)

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
                    avatar = chatPreview.avatar,
                    delivery = chatPreview.content?.delivery,
                )
                Column {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            ConversationName(
                                name = conversationName,
                                read = read,
                            )
                        }
                        if (chatPreview.content?.timestamp != null) {
                            Text(
                                text = chatPreview.content.timestamp.toReadableTime(),
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.alpha(0.8f),
                            )
                        }
                    }
                    PreviewContent(
                        content = chatPreview.content,
                        read = read,
                    )
                }
            }
        }
    }
}

@Composable
private fun ConversationName(
    name: String,
    read: Boolean,
) {
    val defaultNameStyle = MaterialTheme.typography.titleMedium
    val nameStyle = when {
        read -> defaultNameStyle
        else -> defaultNameStyle.copy(fontWeight = FontWeight.Bold)
    }

    Text(
        text = name,
        style = nameStyle,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun PreviewContent(
    content: ChatPreviewContentState?,
    read: Boolean,
) {
    val defaultTextStyle = MaterialTheme.typography.bodySmall

    val textStyle = when {
        read -> defaultTextStyle
        else -> defaultTextStyle.copy(fontWeight = FontWeight.SemiBold)
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
            SenderIcon(content?.senderState)
        }
    }
}

@Composable
private fun SenderIcon(senderState: SenderState?) {
    if (senderState is SentState) {
        Icon(
            imageVector = Icons.AutoMirrored.TwoTone.Reply,
            contentDescription = "From you: ",
            modifier = Modifier.size(16.dp),
        )
    } else if (senderState is ReceivedCommunityState) {
        SmallAvatar(
            avatar = senderState.senderAvatar,
            contentDescription = stringResource(
                Res.string.from,
                senderState.senderAvatar.placeholder,
            ),
        )
    }
}