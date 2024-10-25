package com.thebrownfoxx.neon.client.application.ui.screen.conversations.component

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
import com.thebrownfoxx.neon.client.application.ui.component.avatar.state.SingleAvatarState
import com.thebrownfoxx.neon.client.application.ui.extension.toReadableTime
import com.thebrownfoxx.neon.client.application.ui.screen.conversations.state.ConversationPreviewState
import com.thebrownfoxx.neon.client.application.ui.screen.conversations.state.PreviewContentState
import com.thebrownfoxx.neon.client.application.ui.screen.conversations.state.SenderState
import com.thebrownfoxx.neon.client.application.ui.screen.conversations.state.SentByOtherState
import com.thebrownfoxx.neon.client.application.ui.screen.conversations.state.SentBySelfState
import com.thebrownfoxx.neon.client.application.ui.component.delivery.state.DeliveryState
import com.thebrownfoxx.neon.client.application.ui.theme.NeonTheme
import com.thebrownfoxx.neon.common.extension.toLocalDateTime
import kotlinx.datetime.Clock
import neon.client.application.generated.resources.Res
import neon.client.application.generated.resources.deleted_group
import neon.client.application.generated.resources.from
import neon.client.application.generated.resources.start_a_conversation
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ConversationPreview(
    conversationPreview: ConversationPreviewState,
    read: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // TODO: This should ID if it is a deleted community or member
    val conversationName = conversationPreview.name ?: stringResource(Res.string.deleted_group)

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
                    avatar = conversationPreview.avatar,
                    delivery = conversationPreview.content?.delivery,
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
                        if (conversationPreview.content?.timestamp != null) {
                            Text(
                                text = conversationPreview.content.timestamp.toReadableTime(),
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.alpha(0.8f),
                            )
                        }
                    }
                    PreviewContent(
                        content = conversationPreview.content,
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
    content: PreviewContentState?,
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
    if (senderState is SentBySelfState) {
        Icon(
            imageVector = Icons.AutoMirrored.TwoTone.Reply,
            contentDescription = "From you: ",
            modifier = Modifier.size(16.dp),
        )
    } else if (senderState is SentByOtherState) {
        SmallAvatar(
            avatar = senderState.avatar,
            contentDescription = stringResource(
                Res.string.from,
                senderState.avatar.placeholder,
            ),
        )
    }
}

// TODO: Previews
@Preview
@Composable
private fun SentDirectPreview() {
    NeonTheme {
        ConversationPreview(
            conversationPreview = ConversationPreviewState(
                avatar = SingleAvatarState(url = null, placeholder = "John"),
                name = "John",
                content = PreviewContentState(
                    message = "Hello",
                    timestamp = Clock.System.now().toLocalDateTime(),
                    delivery = DeliveryState.Delivered,
                    senderState = SentBySelfState,
                ),
            ),
            read = true,
            modifier = Modifier.fillMaxWidth(),
            onClick = {},
        )
    }
}

//@Preview
//@Composable
//private fun ReceivedDirectPreview() {
//    NeonTheme {
//        ConversationListItem(
//            conversationWithPreview = ConversationsDummy.ReceivedDirectConversation,
//            read = true,
//            modifier = Modifier.fillMaxWidth(),
//            onClick = {},
//        )
//    }
//}
//
//@Preview
//@Composable
//private fun SentCommunityPreview() {
//    NeonTheme {
//        ConversationListItem(
//            conversationWithPreview = ConversationsDummy.SentCommunityConversation,
//            read = true,
//            modifier = Modifier.fillMaxWidth(),
//            onClick = {},
//        )
//    }
//}
//
//@Preview
//@Composable
//private fun ReceivedCommunityPreview() {
//    NeonTheme {
//        ConversationListItem(
//            conversationWithPreview = ConversationsDummy.ReceivedCommunityConversation,
//            read = true,
//            modifier = Modifier.fillMaxWidth(),
//            onClick = {},
//        )
//    }
//}
//
//@Preview
//@Composable
//private fun UnreadPreview() {
//    NeonTheme {
//        ConversationListItem(
//            conversationWithPreview = ConversationsDummy.ReceivedDirectConversation,
//            read = false,
//            modifier = Modifier.fillMaxWidth(),
//            onClick = {},
//        )
//    }
//}