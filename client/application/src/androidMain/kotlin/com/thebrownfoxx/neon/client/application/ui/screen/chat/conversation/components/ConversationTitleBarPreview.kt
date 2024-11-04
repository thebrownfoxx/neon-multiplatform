package com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.thebrownfoxx.neon.client.application.ui.component.avatar.state.SingleAvatarState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.ConversationInfoState
import com.thebrownfoxx.neon.client.application.ui.theme.NeonTheme
import com.thebrownfoxx.neon.common.type.Loaded

@Preview
@Composable
private fun Preview() {
    NeonTheme {
        ConversationTitleBar(
            info = Loaded(
                ConversationInfoState(
                    avatar = SingleAvatarState(
                        url = null,
                        placeholder = "SharlLeclerc",
                    ),
                    name = "SharlLeclerc",
                )
            ),
            onClose = {},
            onCall = {},
        )
    }
}