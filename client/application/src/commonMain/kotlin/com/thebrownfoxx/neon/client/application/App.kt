package com.thebrownfoxx.neon.client.application

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.thebrownfoxx.neon.common.type.Url
import com.thebrownfoxx.neon.client.application.ui.component.avatar.LargeAvatar
import com.thebrownfoxx.neon.client.application.ui.component.avatar.state.SingleAvatarState
import com.thebrownfoxx.neon.client.application.ui.screen.login.LoginScreen
import com.thebrownfoxx.neon.client.application.ui.screen.login.state.LoginScreenEventHandler
import com.thebrownfoxx.neon.client.application.ui.screen.login.state.LoginScreenState
import com.thebrownfoxx.neon.client.application.ui.theme.NeonTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    NeonTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            LoginScreen(
                state = LoginScreenState(),
                eventHandler = LoginScreenEventHandler.Blank,
            )
        }
    }
}