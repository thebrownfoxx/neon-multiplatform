package com.thebrownfoxx.neon.client.application.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.thebrownfoxx.neon.client.application.dependencies
import com.thebrownfoxx.neon.client.application.ui.extension.sharedZAxisEnter
import com.thebrownfoxx.neon.client.application.ui.extension.sharedZAxisExit

@Composable
fun NavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = LoginRoute,
        modifier = modifier.fillMaxSize(),
        enterTransition = { sharedZAxisEnter() },
        exitTransition = { sharedZAxisExit() },
        popEnterTransition = { sharedZAxisEnter(reversed = true) },
        popExitTransition = { sharedZAxisExit(reversed = true) },
    ) {
        loginDestination()
        chatDestination()
    }

    val viewModel = viewModel { NavHostViewModel(dependencies.authenticator) }
    val loggedIn by viewModel.loggedIn.collectAsStateWithLifecycle()

    LaunchedEffect(loggedIn) {
        when {
            loggedIn -> navController.onLogin()
            else -> navController.onLogout()
        }
    }
}