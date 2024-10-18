package com.thebrownfoxx.neon.client.application.ui.screen.login.state

sealed interface LoginState {
    data object Idle : LoginState
    data class CredentialsMissing(
        val usernameMissing: Boolean,
        val passwordMissing: Boolean,
    ) : LoginState
    data object LoggingIn : LoginState
    data object CredentialsIncorrect : LoginState
    data object ConnectionError : LoginState
}