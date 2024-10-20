package com.thebrownfoxx.neon.client.application.ui.screen.login

import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.thebrownfoxx.neon.client.application.ui.extension.LocalWindowSizeClass
import com.thebrownfoxx.neon.client.application.ui.screen.login.state.LoginScreenEventHandler
import com.thebrownfoxx.neon.client.application.ui.screen.login.state.LoginScreenState
import com.thebrownfoxx.neon.client.application.ui.screen.login.variant.CompactLoginScreen
import com.thebrownfoxx.neon.client.application.ui.screen.login.variant.ExpandedLoginScreen
import com.thebrownfoxx.neon.client.application.ui.screen.login.variant.MediumLoginScreen
import com.thebrownfoxx.neon.client.application.ui.theme.NeonTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

// TODO: If the user insists on logging in with missing credentials, reemphasize the warning.
// TODO: If there is an error, open the relevant text field
@Composable
fun LoginScreen(
    state: LoginScreenState,
    eventHandler: LoginScreenEventHandler,
    modifier: Modifier = Modifier,
) {
    val windowSizeClass = LocalWindowSizeClass.current

    when (windowSizeClass?.widthSizeClass) {
        WindowWidthSizeClass.Expanded -> ExpandedLoginScreen(
            state = state,
            eventHandler = eventHandler,
            modifier = modifier,
        )

        WindowWidthSizeClass.Medium -> MediumLoginScreen(
            state = state,
            eventHandler = eventHandler,
            modifier = modifier,
        )

        else -> CompactLoginScreen(
            state = state,
            eventHandler = eventHandler,
            modifier = modifier,
        )
    }
}

@Preview
@Composable
private fun Preview() {
    NeonTheme {
        LoginScreen(
            state = LoginScreenState(),
            eventHandler = LoginScreenEventHandler.Blank,
        )
    }
}