package com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.thebrownfoxx.neon.client.application.ui.component.avatar.state.SingleAvatarState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ChatPreviewState
import com.thebrownfoxx.neon.client.application.ui.theme.NeonTheme

@Preview
@Composable
private fun Preview() {
    NeonTheme {
        ChatPreviewLoader(modifier = Modifier.fillMaxWidth())
    }
}

@Preview
@Composable
private fun SizeComparisonPreview() {
    NeonTheme {
        ChatPreview(
            state = ChatPreviewState(
                name = "little_lando",
                content = null,
                emphasized = false,
                avatar = SingleAvatarState(
                    url = null,
                    placeholder = "little_lando",
                ),
            ),
            onClick = {},
            modifier = Modifier
                .padding(16.dp)
                .background(Color.Red.copy(alpha = 0.2f)),
        )
        ChatPreviewLoader(
            modifier = Modifier
                .padding(16.dp)
                .background(Color.Yellow.copy(alpha = 0.2f)),
        )
    }
}