package com.thebrownfoxx.neon.client.application.ui.screen.login.state

class LoginScreenEventHandler(
    val onUsernameChange: (String) -> Unit,
    val onPasswordChange: (String) -> Unit,
    val onLogin: () -> Unit,
    val onJoin: () -> Unit,
) {
    companion object {
        val Blank = LoginScreenEventHandler(
            onUsernameChange = {},
            onPasswordChange = {},
            onLogin = {},
            onJoin = {},
        )
    }
}