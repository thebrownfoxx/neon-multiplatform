package com.thebrownfoxx.neon.client.application.ui.screen.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thebrownfoxx.neon.client.application.ui.screen.login.state.LoginScreenNavigation
import com.thebrownfoxx.neon.client.application.ui.screen.login.state.LoginScreenState
import com.thebrownfoxx.neon.client.application.ui.screen.login.state.LoginState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
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

            this@LoginViewModel.mutableState.update {
                it.copy(loginState = LoginState.LoggingIn)
            }

            TODO()
//            val result = authenticator.login(
//                username = this@LoginViewModel.mutableState.value.username,
//                password = this@LoginViewModel.mutableState.value.password,
//            )
//
//            when (result) {
//                LoginResult.Successful -> this@LoginViewModel.mutableState.update {
//                    it.copy(loginState = LoginState.Idle)
//                }
//
//                LoginResult.CredentialsIncorrect -> this@LoginViewModel.mutableState.update {
//                    it.copy(loginState = LoginState.CredentialsIncorrect)
//                }
//
//                LoginResult.ConnectionError -> this@LoginViewModel.mutableState.update {
//                    it.copy(loginState = LoginState.ConnectionError)
//                }
//            }
        }
    }

    fun onJoin() {
        viewModelScope.launch {
            navigation.emit(LoginScreenNavigation.Join)
        }
    }
}