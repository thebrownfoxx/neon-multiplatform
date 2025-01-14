package com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.thebrownfoxx.neon.client.application.ui.component.common.Button
import com.thebrownfoxx.neon.client.application.ui.extension.padding
import com.thebrownfoxx.neon.client.application.ui.extension.rememberMutableStateOf
import com.thebrownfoxx.neon.client.application.ui.theme.NeonTheme

@Preview
@Composable
private fun ChatGroupPreview() {
    NeonTheme {
        MessageListLoader(
            contentPadding = 16.dp.padding,
            isCommunity = false,
        )
    }
}

@Preview
@Composable
private fun CommunityPreview() {
    NeonTheme {
        MessageListLoader(
            contentPadding = 16.dp.padding,
            isCommunity = true,
        )
    }
}

@Preview
@Composable
private fun ToggleableCommunityPreview() {
    var isCommunity by rememberMutableStateOf(false)

    NeonTheme {
        Column {
            MessageListLoader(
                contentPadding = 16.dp.padding,
                isCommunity = isCommunity,
            )
            Button(onClick = { isCommunity = !isCommunity }) {
                Text(text = "Toggle")
            }
        }
    }
}