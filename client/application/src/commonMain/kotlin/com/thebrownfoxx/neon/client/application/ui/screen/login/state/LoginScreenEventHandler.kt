package com.thebrownfoxx.neon.client.application.ui.screen.login.state

class LoginScreenEventHandler(
    val onUsernameChange: (String) -> Unit,
    val onPasswordChange: (String) -> Unit,
    val onLoginClick: () -> Unit,
    val onJoinClick: () -> Unit,
) {
    companion object {
        val Blank = LoginScreenEventHandler(
            onUsernameChange = {},
            onPasswordChange = {},
            onLoginClick = {},
            onJoinClick = {},
        )
    }
}