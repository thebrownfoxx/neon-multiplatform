package com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.thebrownfoxx.neon.client.application.ui.theme.NeonTheme

@Preview
@Composable
private fun Preview() {
    NeonTheme {
        FakeSearchBar()
    }
}