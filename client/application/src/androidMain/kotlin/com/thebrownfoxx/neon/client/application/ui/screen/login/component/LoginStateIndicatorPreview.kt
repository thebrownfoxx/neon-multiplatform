package com.thebrownfoxx.neon.client.application.ui.screen.login.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.thebrownfoxx.neon.client.application.ui.extension.padding
import com.thebrownfoxx.neon.client.application.ui.screen.login.state.LoginState.ConnectionError
import com.thebrownfoxx.neon.client.application.ui.screen.login.state.LoginState.CredentialsIncorrect
import com.thebrownfoxx.neon.client.application.ui.screen.login.state.LoginState.CredentialsMissing
import com.thebrownfoxx.neon.client.application.ui.screen.login.state.LoginState.Idle
import com.thebrownfoxx.neon.client.application.ui.screen.login.state.LoginState.LoggingIn
import com.thebrownfoxx.neon.client.application.ui.theme.NeonTheme

@Preview
@Composable
private fun IdleLoginPreview() {
    NeonTheme {
        LoginStateIndicator(
            state = Idle,
            padding = 16.dp.padding,
        )
    }
}

@Preview
@Composable
private fun CredentialsMissingPreview() {
    NeonTheme {
        LoginStateIndicator(
            state = CredentialsMissing(
                usernameMissing = true,
                passwordMissing = true,
            ),
            padding = 16.dp.padding,
        )
    }
}

@Preview
@Composable
private fun LoggingInPreview() {
    NeonTheme {
        LoginStateIndicator(
            state = LoggingIn,
            padding = 16.dp.padding,
        )
    }
}

@Preview
@Composable
private fun CredentialsIncorrectPreview() {
    NeonTheme {
        LoginStateIndicator(
            state = CredentialsIncorrect,
            padding = 16.dp.padding,
        )
    }
}

@Preview
@Composable
private fun ConnectionErrorPreview() {
    NeonTheme {
        LoginStateIndicator(
            state = ConnectionError,
            padding = 16.dp.padding,
        )
    }
}