package com.thebrownfoxx.neon.client.application.ui.component.scrim

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.thebrownfoxx.neon.client.application.ui.theme.NeonTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun GradientScrimBox(
    gradientDirection: GradientDirection,
    scrimColor: Color,
    modifier: Modifier = Modifier,
    maxAlpha: Float = 1f,
    threshold: Float = 0.5f,
    thresholdAlpha: Float = maxAlpha / 2,
    content: @Composable BoxScope.() -> Unit,
) {
    val scrim = gradientScrimBrush(
        direction = gradientDirection,
        color = scrimColor,
        maxAlpha = maxAlpha,
        threshold = threshold,
        thresholdAlpha = thresholdAlpha,
    )

    Box(modifier = modifier.background(scrim)) {
        content()
    }
}

@Preview
@Composable
private fun Preview() {
    NeonTheme {
        Surface {
            Box {
//                Text(text = LoremIpsum(50).values.first())
                // TODO: find a substitute for LoremIpsum
                GradientScrimBox(
                    gradientDirection = GradientDirection.TopBottom,
                    scrimColor = MaterialTheme.colorScheme.surface,
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.padding(16.dp),
                    ) {
                        Text(
                            text = "Some header",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}