package com.thebrownfoxx.neon.client.application.ui.screen.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.tooling.preview.Preview
import com.thebrownfoxx.neon.client.application.ui.extension.CompactWindowSizeClass
import com.thebrownfoxx.neon.client.application.ui.extension.LocalWindowSizeClass
import com.thebrownfoxx.neon.client.application.ui.extension.MediumWindowSizeClass
import com.thebrownfoxx.neon.client.application.ui.screen.login.state.LoginScreenEventHandler
import com.thebrownfoxx.neon.client.application.ui.screen.login.state.LoginScreenState
import com.thebrownfoxx.neon.client.application.ui.theme.NeonTheme

@Preview
@Composable
private fun CompactPreview() {
    NeonTheme {
        CompositionLocalProvider(LocalWindowSizeClass provides CompactWindowSizeClass) {
            LoginScreen(
                state = LoginScreenState(),
                eventHandler = LoginScreenEventHandler.Blank,
            )
        }
    }
}

@Preview(device = "spec:width=673dp,height=841dp")
@Composable
private fun WidePreview() {
    NeonTheme {
        CompositionLocalProvider(LocalWindowSizeClass provides MediumWindowSizeClass) {
            LoginScreen(
                state = LoginScreenState(),
                eventHandler = LoginScreenEventHandler.Blank,
            )
        }
    }
}