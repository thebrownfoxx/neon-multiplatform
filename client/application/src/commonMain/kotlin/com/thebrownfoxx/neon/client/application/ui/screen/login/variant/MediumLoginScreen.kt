package com.thebrownfoxx.neon.client.application.ui.screen.login.variant

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import com.thebrownfoxx.neon.client.application.ui.extension.NavigationBarHeight
import com.thebrownfoxx.neon.client.application.ui.extension.PaddingSide
import com.thebrownfoxx.neon.client.application.ui.extension.paddingExcept
import com.thebrownfoxx.neon.client.application.ui.screen.login.component.LoginScreenContent
import com.thebrownfoxx.neon.client.application.ui.screen.login.state.LoginScreenEventHandler
import com.thebrownfoxx.neon.client.application.ui.screen.login.state.LoginScreenState
import com.thebrownfoxx.neon.client.application.ui.theme.NeonTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun MediumLoginScreen(
    state: LoginScreenState,
    eventHandler: LoginScreenEventHandler,
    modifier: Modifier = Modifier,
) {
    val bottomPadding = max(NavigationBarHeight, 24.dp)

    Surface(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState(), reverseScrolling = true)
                .statusBarsPadding()
                .imePadding()
                .padding(24.dp.paddingExcept(PaddingSide.Bottom))
                .padding(bottom = bottomPadding)
                .consumeWindowInsets(WindowInsets.navigationBars),
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.Center)
                    .sizeIn(maxWidth = 512.dp),
                shape = RoundedCornerShape(24.dp),
            ) {
                LoginScreenContent(
                    state = state,
                    eventHandler = eventHandler,
                    modifier = Modifier.padding(24.dp),
                )
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    NeonTheme {
        MediumLoginScreen(
            state = LoginScreenState(),
            eventHandler = LoginScreenEventHandler.Blank,
        )
    }
}