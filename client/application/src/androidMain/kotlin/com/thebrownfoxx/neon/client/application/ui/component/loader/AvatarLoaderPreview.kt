package com.thebrownfoxx.neon.client.application.ui.component.loader

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.thebrownfoxx.neon.client.application.ui.theme.NeonTheme

@Preview
@Composable
private fun Preview() {
    NeonTheme {
        LargeAvatarLoader(modifier = Modifier.padding(16.dp))
    }
}