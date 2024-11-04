package com.thebrownfoxx.neon.client.application.ui.screen.login.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.max
import com.thebrownfoxx.neon.client.application.ui.extension.NavigationBarHeight
import com.thebrownfoxx.neon.client.application.ui.extension.PaddingSide
import com.thebrownfoxx.neon.client.application.ui.extension.bottomDp
import com.thebrownfoxx.neon.client.application.ui.extension.except

@Composable
fun LoginScreenBox(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    content: @Composable BoxScope.() -> Unit,
) {
    val bottomPadding = max(NavigationBarHeight, contentPadding.bottomDp)

    Box(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState(), reverseScrolling = true)
            .statusBarsPadding()
            .imePadding()
            .padding(contentPadding.except(PaddingSide.Bottom))
            .padding(bottom = bottomPadding)
            .consumeWindowInsets(WindowInsets.navigationBars),
        content = content,
    )
}