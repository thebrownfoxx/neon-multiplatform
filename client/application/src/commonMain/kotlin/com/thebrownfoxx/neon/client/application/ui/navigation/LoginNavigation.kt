package com.thebrownfoxx.neon.client.application.ui.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.thebrownfoxx.neon.client.application.dependencies
import com.thebrownfoxx.neon.client.application.ui.screen.login.LoginScreen
import com.thebrownfoxx.neon.client.application.ui.screen.login.LoginViewModel
import com.thebrownfoxx.neon.client.application.ui.screen.login.state.LoginScreenEventHandler
import kotlinx.serialization.Serializable

@Serializable
data object LoginRoute

fun NavGraphBuilder.loginDestination() = composable<LoginRoute> {
    val viewModel = viewModel { LoginViewModel(dependencies.authenticator) }
    with(viewModel) {
        val loginScreenState by state.collectAsStateWithLifecycle()
        val loginScreenEventHandler = LoginScreenEventHandler(
            onUsernameChange = ::onUsernameChange,
            onPasswordChange = ::onPasswordChange,
            onLogin = ::onLogin,
            onJoin = ::onJoin,
        )

        LoginScreen(
            state = loginScreenState,
            eventHandler = loginScreenEventHandler,
        )
    }
}

fun NavController.onLogout() = navigate(LoginRoute) {
    popUpTo(graph.startDestinationId) { inclusive = true }
    launchSingleTop = true
}

fun NavController.onLogin() = navigate(ChatRoute) {
    popUpTo(graph.startDestinationId) { inclusive = true }
    launchSingleTop = true
}