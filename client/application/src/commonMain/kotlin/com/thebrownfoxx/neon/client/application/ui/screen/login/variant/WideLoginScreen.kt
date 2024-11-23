package com.thebrownfoxx.neon.client.application.ui.screen.login.variant

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
fun WideLoginScreen(
    state: LoginScreenState,
    eventHandler: LoginScreenEventHandler,
    modifier: Modifier = Modifier,
) {
    val contentPadding = 48.dp.padding

    Surface(modifier = modifier) {
        LoginScreenBox(contentPadding = contentPadding) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.Center)
                    .sizeIn(maxWidth = 512.dp),
                shape = RoundedCornerShape(24.dp),
            ) {
                LoginScreenContent(
                    state = state,
                    eventHandler = eventHandler,
                    modifier = Modifier.padding(contentPadding),
                )
            }
        }
    }
}