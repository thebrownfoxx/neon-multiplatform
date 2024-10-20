package com.thebrownfoxx.neon.client.application.ui.screen.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thebrownfoxx.neon.client.application.ui.screen.login.state.LoginScreenNavigation
import com.thebrownfoxx.neon.client.application.ui.screen.login.state.LoginScreenState
import com.thebrownfoxx.neon.client.application.ui.screen.login.state.LoginState
import com.thebrownfoxx.neon.client.service.authenticator.Authenticator
import com.thebrownfoxx.neon.client.service.authenticator.model.LoginError
import com.thebrownfoxx.neon.common.model.onFailure
import com.thebrownfoxx.neon.common.model.onSuccess
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(private val authenticator: Authenticator) : ViewModel() {
    private val navigation = MutableSharedFlow<LoginScreenNavigation>()

    private val mutableState = MutableStateFlow(LoginScreenState())
    val state = mutableState.asStateFlow()

    fun onUsernameChange(newUsername: String) {
        mutableState.update { it.copy(username = newUsername) }
    }

    fun onPasswordChange(newPassword: String) {
        mutableState.update { it.copy(password = newPassword) }
    }

    fun onLogin() {
        viewModelScope.launch {
            val state = mutableState.value

            val usernameMissing = state.username.isBlank()
            val passwordMissing = state.password.isEmpty()
            if (usernameMissing || passwordMissing) {
                this@LoginViewModel.mutableState.update {
                    it.copy(
                        loginState = LoginState.CredentialsMissing(
                            usernameMissing = usernameMissing,
                            passwordMissing = passwordMissing,
                        )
                    )
                }
                return@launch
            }

            mutableState.update {
                it.copy(loginState = LoginState.LoggingIn)
            }

            val result = authenticator.login(
                username = mutableState.value.username,
                password = mutableState.value.password,
            )

            result.onFailure { error ->
                when (error) {
                    LoginError.InvalidCredentials ->
                        mutableState.update { it.copy(loginState = LoginState.CredentialsIncorrect) }

                    LoginError.ConnectionError ->
                        mutableState.update { it.copy(loginState = LoginState.ConnectionError) }

                    LoginError.UnknownError ->
                        mutableState.update { it.copy(loginState = LoginState.ConnectionError) }
                }
            }

            result.onSuccess {
                mutableState.update { it.copy(loginState = LoginState.Idle) }
            }
        }
    }

    fun onJoin() {
        viewModelScope.launch {
            navigation.emit(LoginScreenNavigation.Join)
        }
    }
}