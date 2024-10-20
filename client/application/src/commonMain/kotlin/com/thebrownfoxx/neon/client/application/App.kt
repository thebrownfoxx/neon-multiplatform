package com.thebrownfoxx.neon.client.application

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thebrownfoxx.neon.client.application.ui.extension.LocalWindowSizeClass
import com.thebrownfoxx.neon.client.application.ui.extension.calculateWindowSizeClass
import com.thebrownfoxx.neon.client.application.ui.screen.login.LoginScreen
import com.thebrownfoxx.neon.client.application.ui.screen.login.LoginViewModel
import com.thebrownfoxx.neon.client.application.ui.screen.login.state.LoginScreenEventHandler
import com.thebrownfoxx.neon.client.application.ui.theme.NeonTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.thebrownfoxx.neon.client.service.default.DefaultAuthenticator
import com.thebrownfoxx.neon.client.service.default.InMemoryTokenStorage
import io.ktor.client.HttpClient

@Composable
@Preview
fun App() {
    CompositionLocalProvider(LocalWindowSizeClass provides calculateWindowSizeClass()) {
        NeonTheme {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                val loginViewModel = viewModel {
                    val authenticator = DefaultAuthenticator(
                        HttpClient(),
                        InMemoryTokenStorage(),
                    )
                    LoginViewModel(authenticator)
                }
                val loginScreenState by loginViewModel.state.collectAsStateWithLifecycle()
                val loginScreenEventHandler = with(loginViewModel) {
                    LoginScreenEventHandler(
                        onUsernameChange = ::onUsernameChange,
                        onPasswordChange = ::onPasswordChange,
                        onLogin = ::onLogin,
                        onJoin = ::onJoin,
                    )
                }

                LoginScreen(
                    state = loginScreenState,
                    eventHandler = loginScreenEventHandler,
                )
            }
        }
    }
}