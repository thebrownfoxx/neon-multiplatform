package com.thebrownfoxx.neon.client.application.ui.component.twopane

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thebrownfoxx.neon.client.application.ui.extension.RoundedCorners

@Composable
fun Pane(
    modifier: Modifier = Modifier,
    roundedCorners: RoundedCorners = RoundedCorners.All,
    content: @Composable () -> Unit,
) {
    Surface(
        shape = roundedCorners.toShape(roundRadius = 16.dp, nonRoundRadius = 8.dp),
        modifier = modifier,
    ) {
        content()
    }
}