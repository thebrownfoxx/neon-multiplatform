package com.thebrownfoxx.neon.client.application

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.thebrownfoxx.neon.client.application.ui.extension.LocalWindowSizeClass
import com.thebrownfoxx.neon.client.application.ui.extension.calculateWindowSizeClass
import com.thebrownfoxx.neon.client.application.ui.navigation.NavHost
import com.thebrownfoxx.neon.client.application.ui.theme.NeonTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    CompositionLocalProvider(LocalWindowSizeClass provides calculateWindowSizeClass()) {
        NeonTheme {
            NavHost()
        }
    }
}