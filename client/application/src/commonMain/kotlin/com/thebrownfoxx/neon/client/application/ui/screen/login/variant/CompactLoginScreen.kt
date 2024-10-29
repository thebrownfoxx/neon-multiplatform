package com.thebrownfoxx.neon.client.application.ui.screen.login.variant

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thebrownfoxx.neon.client.application.ui.extension.padding
import com.thebrownfoxx.neon.client.application.ui.screen.login.component.LoginScreenBox
import com.thebrownfoxx.neon.client.application.ui.screen.login.component.LoginScreenContent
import com.thebrownfoxx.neon.client.application.ui.screen.login.state.LoginScreenEventHandler
import com.thebrownfoxx.neon.client.application.ui.screen.login.state.LoginScreenState

@Composable
fun CompactLoginScreen(
    state: LoginScreenState,
    eventHandler: LoginScreenEventHandler,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier) {
        LoginScreenBox(contentPadding = 16.dp.padding) {
            LoginScreenContent(
                state = state,
                eventHandler = eventHandler,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}