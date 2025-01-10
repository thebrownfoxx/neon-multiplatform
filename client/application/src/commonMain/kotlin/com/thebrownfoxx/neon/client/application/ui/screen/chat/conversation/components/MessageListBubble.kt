package com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.thebrownfoxx.neon.client.application.ui.component.Spacer
import com.thebrownfoxx.neon.client.application.ui.component.avatar.AvatarSize
import com.thebrownfoxx.neon.client.application.ui.component.avatar.MediumAvatar
import com.thebrownfoxx.neon.client.application.ui.component.avatar.state.SingleAvatarState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.GroupPosition

@Composable
fun SentMessageListBubble(
    content: String,
    groupPosition: GroupPosition,
    modifier: Modifier = Modifier,
) {
    SentMessageLayout(modifier = modifier.fillMaxWidth()) {
        SentMessageBubble(
            content = content,
            groupPosition = groupPosition,
        )
    }
}

@Composable
fun ReceivedDirectMessageListBubble(
    content: String,
    groupPosition: GroupPosition,
    read: Boolean,
    modifier: Modifier = Modifier,
) {
    ReceivedMessageLayout(modifier = modifier.fillMaxWidth()) {
        ReceivedMessageBubble(
            content = content,
            groupPosition = groupPosition,
            read = read,
        )
    }
}

@Composable
fun ReceivedCommunityMessageListBubble(
    senderAvatar: SingleAvatarState?,
    content: String,
    groupPosition: GroupPosition,
    read: Boolean,
    modifier: Modifier = Modifier,
) {
    ReceivedCommunityMessageLayout(
        avatar = {
            when (groupPosition) {
                GroupPosition.Last, GroupPosition.Alone -> MediumAvatar(avatar = senderAvatar)
                else -> Spacer(width = AvatarSize.Medium.dp)
            }
        },
        modifier = modifier.fillMaxWidth(),
    ) {
        ReceivedMessageBubble(
            content = content,
            read = read,
            groupPosition = groupPosition,
        )
    }
}