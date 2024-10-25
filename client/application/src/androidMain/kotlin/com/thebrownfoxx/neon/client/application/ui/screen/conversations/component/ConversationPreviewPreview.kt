package com.thebrownfoxx.neon.client.application.ui.screen.conversations.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.thebrownfoxx.neon.client.application.ui.component.avatar.state.SingleAvatarState
import com.thebrownfoxx.neon.client.application.ui.component.delivery.state.DeliveryState
import com.thebrownfoxx.neon.client.application.ui.screen.conversations.state.ConversationPreviewState
import com.thebrownfoxx.neon.client.application.ui.screen.conversations.state.PreviewContentState
import com.thebrownfoxx.neon.client.application.ui.screen.conversations.state.SentBySelfState
import com.thebrownfoxx.neon.client.application.ui.theme.NeonTheme
import com.thebrownfoxx.neon.common.extension.toLocalDateTime
import kotlinx.datetime.Clock

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