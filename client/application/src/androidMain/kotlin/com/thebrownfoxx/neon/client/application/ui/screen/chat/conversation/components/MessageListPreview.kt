package com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.thebrownfoxx.neon.client.application.ui.screen.chat.state.ConversationDummy
import com.thebrownfoxx.neon.client.application.ui.theme.NeonTheme

@Preview
@Composable
private fun LoadingPreview() {
    NeonTheme {
        MessageList(
            entries = emptyList(),
            isCommunity = false,
            loading = true,
            onMarkAsRead = {},
            onLastVisibleEntryChange = {},
        )
    }
}

@Preview
@Composable
private fun MemberPreview() {
    NeonTheme {
        MessageList(
            entries = ConversationDummy.DirectMessageEntries,
            isCommunity = false,
            loading = false,
            onMarkAsRead = {},
            onLastVisibleEntryChange = {},
        )
    }
}

@Preview
@Composable
private fun CommunityPreview() {
    NeonTheme {
        MessageList(
            entries = ConversationDummy.CommunityMessageEntries,
            isCommunity = true,
            loading = false,
            onMarkAsRead = {},
            onLastVisibleEntryChange = {},
        )
    }
}