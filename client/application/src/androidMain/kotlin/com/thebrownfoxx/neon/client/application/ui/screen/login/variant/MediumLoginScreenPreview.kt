package com.thebrownfoxx.neon.client.application.ui.screen.login.variant

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.thebrownfoxx.neon.client.application.ui.screen.login.state.LoginScreenEventHandler
import com.thebrownfoxx.neon.client.application.ui.screen.login.state.LoginScreenState
import com.thebrownfoxx.neon.client.application.ui.theme.NeonTheme

@Preview(device = "spec:width=1280dp,height=800dp,dpi=240")
@Composable
private fun Preview() {
    NeonTheme {
        MediumLoginScreen(
            state = LoginScreenState(),
            eventHandler = LoginScreenEventHandler.Blank,
        )
    }
}