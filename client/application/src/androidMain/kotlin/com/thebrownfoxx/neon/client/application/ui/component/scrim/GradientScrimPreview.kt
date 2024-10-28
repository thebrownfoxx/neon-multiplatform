package com.thebrownfoxx.neon.client.application.ui.component.scrim

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview
@Composable
private fun Preview() {
    GradientScrim(
        direction = GradientDirection.TopBottom,
        color = Color.Black,
        modifier = Modifier
            .height(16.dp)
            .fillMaxWidth(),
    )
}