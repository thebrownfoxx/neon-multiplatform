package com.thebrownfoxx.neon.client.application.ui.screen.login.state

sealed interface LoginState {
    data object Idle : LoginState
    data class CredentialsMissing(val missingCredential: MissingCredential) : LoginState
    data object LoggingIn : LoginState
    data object CredentialsIncorrect : LoginState
    data object ConnectionError : LoginState
    data object UnknownError : LoginState
}

enum class MissingCredential {
    Username,
    Password,
    Both,
}