package com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.thebrownfoxx.neon.client.application.ui.extension.padding
import com.thebrownfoxx.neon.client.application.ui.theme.NeonTheme

@Preview
@Composable
private fun Preview() {
    NeonTheme {
        DirectMessageListLoader(contentPadding = 16.dp.padding)
    }
}