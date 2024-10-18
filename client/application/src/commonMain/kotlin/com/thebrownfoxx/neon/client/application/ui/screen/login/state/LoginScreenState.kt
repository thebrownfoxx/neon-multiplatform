package com.thebrownfoxx.neon.client.application.ui.screen.login.state

data class LoginScreenState(
    val username: String = "",
    val password: String = "",
    val loginState: LoginState = LoginState.Idle,
)