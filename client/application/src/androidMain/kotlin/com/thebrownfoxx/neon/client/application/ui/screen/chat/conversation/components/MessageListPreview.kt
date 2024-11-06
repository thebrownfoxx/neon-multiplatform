package com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.thebrownfoxx.neon.client.application.ui.screen.chat.state.ConversationDummy
import com.thebrownfoxx.neon.client.application.ui.theme.NeonTheme
import com.thebrownfoxx.neon.common.type.Loaded
import com.thebrownfoxx.neon.common.type.Loading

@Preview
@Composable
private fun LoadingPreview() {
    NeonTheme {
        MessageList(
            entries = Loading,
            onMarkAsRead = {},
        )
    }
}

@Preview
@Composable
private fun MemberPreview() {
    NeonTheme {
        MessageList(
            entries = Loaded(ConversationDummy.DirectMessageEntries),
            onMarkAsRead = {},
        )
    }
}

@Preview
@Composable
private fun CommunityPreview() {
    NeonTheme {
        MessageList(
            entries = Loaded(ConversationDummy.CommunityMessageEntries),
            onMarkAsRead = {},
        )
    }
}