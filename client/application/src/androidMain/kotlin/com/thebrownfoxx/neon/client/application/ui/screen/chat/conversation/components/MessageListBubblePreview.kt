package com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.components

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.thebrownfoxx.neon.client.application.ui.component.avatar.state.SingleAvatarState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.GroupPosition
import com.thebrownfoxx.neon.client.application.ui.theme.NeonTheme

@Preview
@Composable
private fun SentPreview() {
    NeonTheme {
        SentMessageListBubble(
            content = "Hey, that's great!",
            groupPosition = GroupPosition.Alone,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview
@Composable
private fun ReceivedPreview() {
    NeonTheme {
        ReceivedDirectMessageListBubble(
            content = "Hey, that's great!",
            groupPosition = GroupPosition.Alone,
            read = false,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview
@Composable
private fun FirstReceivedCommunityPreview() {
    NeonTheme {
        ReceivedCommunityMessageListBubble(
            senderAvatar = SingleAvatarState(
                url = null,
                placeholder = "Person"
            ),
            content = "Hey, that's great!",
            groupPosition = GroupPosition.First,
            read = false,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview
@Composable
private fun MiddleReceivedCommunityPreview() {
    NeonTheme {
        ReceivedCommunityMessageListBubble(
            senderAvatar = SingleAvatarState(
                url = null,
                placeholder = "Person"
            ),
            content = "Hey, that's great!",
            groupPosition = GroupPosition.Middle,
            read = false,
            modifier = Modifier.padding(16.dp),
        )
    }
}