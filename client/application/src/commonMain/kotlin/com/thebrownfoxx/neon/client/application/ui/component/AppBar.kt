package com.thebrownfoxx.neon.client.application.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import com.thebrownfoxx.neon.client.application.ui.component.scrim.GradientDirection
import com.thebrownfoxx.neon.client.application.ui.component.scrim.GradientScrimBox
import com.thebrownfoxx.neon.client.application.ui.extension.NavigationBarHeight
import com.thebrownfoxx.neon.client.application.ui.extension.PaddingSide
import com.thebrownfoxx.neon.client.application.ui.extension.StatusBarPadding
import com.thebrownfoxx.neon.client.application.ui.extension.bottomPadding
import com.thebrownfoxx.neon.client.application.ui.extension.paddingExcept
import com.thebrownfoxx.neon.client.application.ui.extension.plus
import com.thebrownfoxx.neon.client.application.ui.extension.topPadding

object AppBarDefaults {
    val ContainerColor
        @Composable get() = MaterialTheme.colorScheme.surfaceContainer

    val ContainerAlpha @Composable get() = 0.9f

    val ScrimColor @Composable get() = MaterialTheme.colorScheme.surface

    val TopBarPadding @Composable get() = 16.dp.paddingExcept(PaddingSide.Top) + 8.dp.topPadding +
            StatusBarPadding

    val BottomBarPadding
        @Composable get() =
            16.dp.paddingExcept(PaddingSide.Bottom) + max(16.dp, NavigationBarHeight).bottomPadding
}

@Composable
private fun AppBarScrim(
    gradientDirection: GradientDirection,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    scrimColor: Color = AppBarDefaults.ScrimColor,
    content: @Composable () -> Unit,
) {
    GradientScrimBox(
        gradientDirection = gradientDirection,
        scrimColor = scrimColor,
        modifier = modifier,
        maxAlpha = 0.9f,
        threshold = 0.4f,
        thresholdAlpha = 0.7f,
    ) {
        Box(modifier = Modifier.padding(contentPadding)) {
            content()
        }
    }
}

@Composable
fun TopBarScrim(
    modifier: Modifier = Modifier,
    scrimColor: Color = AppBarDefaults.ScrimColor,
    contentPadding: PaddingValues = AppBarDefaults.TopBarPadding,
    content: @Composable () -> Unit,
) {
    AppBarScrim(
        gradientDirection = GradientDirection.TopBottom,
        contentPadding = contentPadding,
        scrimColor = scrimColor,
        modifier = modifier.consumeWindowInsets(WindowInsets.statusBars),
        content = content,
    )
}

@Composable
fun BottomBarScrim(
    modifier: Modifier = Modifier,
    scrimColor: Color = AppBarDefaults.ScrimColor,
    contentPadding: PaddingValues = AppBarDefaults.BottomBarPadding,
    content: @Composable () -> Unit,
) {
    AppBarScrim(
        gradientDirection = GradientDirection.BottomTop,
        contentPadding = contentPadding,
        scrimColor = scrimColor,
        modifier = modifier.consumeWindowInsets(WindowInsets.navigationBars),
        content = content,
    )
}

@Composable
fun AppBar(
    modifier: Modifier = Modifier,
    containerColor: Color = AppBarDefaults.ContainerColor,
    contentColor: Color = contentColorFor(containerColor),
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = containerColor.copy(alpha = AppBarDefaults.ContainerAlpha),
        contentColor = contentColor,
        shape = MaterialTheme.shapes.medium,
        content = content,
    )
}