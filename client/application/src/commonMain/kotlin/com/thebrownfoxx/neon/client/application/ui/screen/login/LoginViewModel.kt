package com.thebrownfoxx.neon.client.application.ui.screen.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thebrownfoxx.neon.client.application.ui.screen.login.state.LoginScreenNavigation
import com.thebrownfoxx.neon.client.application.ui.screen.login.state.LoginScreenState
import com.thebrownfoxx.neon.client.application.ui.screen.login.state.LoginState
import com.thebrownfoxx.neon.client.application.ui.screen.login.state.MissingCredential
import com.thebrownfoxx.neon.client.service.Authenticator
import com.thebrownfoxx.neon.client.service.Authenticator.LoginError
import com.thebrownfoxx.outcome.map.onFailure
import com.thebrownfoxx.outcome.map.onSuccess
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(private val authenticator: Authenticator) : ViewModel() {
    private val navigation = MutableSharedFlow<LoginScreenNavigation>()

    private val _state = MutableStateFlow(LoginScreenState())
    val state = _state.asStateFlow()

    fun onUsernameChange(newUsername: String) {
        _state.update { it.copy(username = newUsername) }
    }

    fun onPasswordChange(newPassword: String) {
        _state.update { it.copy(password = newPassword) }
    }

    fun onLogin() {
        viewModelScope.launch {
            val state = _state.value

            val usernameMissing = state.username.isBlank()
            val passwordMissing = state.password.isEmpty()
            val missingCredential = when {
                usernameMissing && passwordMissing -> MissingCredential.Both
                usernameMissing -> MissingCredential.Username
                passwordMissing -> MissingCredential.Password
                else -> null
            }
            if (missingCredential != null) {
                this@LoginViewModel._state.update {
                    it.copy(loginState = LoginState.CredentialsMissing(missingCredential))
                }
                return@launch
            }

            _state.update {
                it.copy(loginState = LoginState.LoggingIn)
            }

            val outcome = authenticator.login(
                username = _state.value.username,
                password = _state.value.password,
            )

            outcome.onFailure { error ->
                when (error) {
                    LoginError.InvalidCredentials ->
                        _state.update { it.copy(loginState = LoginState.CredentialsIncorrect) }

                    LoginError.ConnectionError ->
                        _state.update { it.copy(loginState = LoginState.ConnectionError) }

                    LoginError.UnexpectedError ->
                        _state.update { it.copy(loginState = LoginState.UnknownError) }
                }
            }

            outcome.onSuccess {
                _state.update { it.copy(loginState = LoginState.Idle) }
            }
        }
    }

    fun onJoin() {
        viewModelScope.launch {
            navigation.emit(LoginScreenNavigation.Join)
        }
    }
}