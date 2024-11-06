package com.thebrownfoxx.neon.client.application.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.thebrownfoxx.neon.client.application.ui.component.scrim.GradientDirection
import com.thebrownfoxx.neon.client.application.ui.component.scrim.GradientScrimBox
import com.thebrownfoxx.neon.client.application.ui.extension.padding

object AppBarDefaults {
    val ContainerColor @Composable get() = MaterialTheme.colorScheme.surfaceContainer
    val ContainerAlpha @Composable get() = 0.9f
    val ScrimColor @Composable get() = MaterialTheme.colorScheme.surface
    val Padding @Composable get() = 16.dp.padding
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
        shape = CircleShape,
        content = content,
    )
}

@Composable
fun TopBarScrim(
    modifier: Modifier = Modifier,
    scrimColor: Color = AppBarDefaults.ScrimColor,
    contentPadding: PaddingValues = AppBarDefaults.Padding,
    content: @Composable () -> Unit,
) {
    AppBarScrim(
        gradientDirection = GradientDirection.TopBottom,
        scrimColor = scrimColor,
        modifier = modifier,
        contentPadding = contentPadding,
        content = content,
    )
}

@Composable
fun BottomBarScrim(
    modifier: Modifier = Modifier,
    scrimColor: Color = AppBarDefaults.ScrimColor,
    contentPadding: PaddingValues = AppBarDefaults.Padding,
    content: @Composable () -> Unit,
) {
    AppBarScrim(
        gradientDirection = GradientDirection.BottomTop,
        scrimColor = scrimColor,
        modifier = modifier,
        contentPadding = contentPadding,
        content = content,
    )
}

@Composable
private fun AppBarScrim(
    gradientDirection: GradientDirection,
    modifier: Modifier = Modifier,
    scrimColor: Color = AppBarDefaults.ScrimColor,
    contentPadding: PaddingValues = AppBarDefaults.Padding,
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
