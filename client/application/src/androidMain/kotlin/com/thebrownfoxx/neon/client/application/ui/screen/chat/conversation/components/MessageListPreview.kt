package com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.thebrownfoxx.neon.client.application.ui.extension.padding
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.ConversationDummy
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
            contentPadding = 16.dp.padding,
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
            contentPadding = 16.dp.padding,
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
            contentPadding = 16.dp.padding,
        )
    }
}