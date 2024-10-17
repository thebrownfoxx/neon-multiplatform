package com.thebrownfoxx.neon.client.application.ui.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

val Shapes = Shapes(
    extraSmall = CutCornerShape(topEnd = 4.dp),
    small = CutCornerShape(topEnd = 8.dp),
    medium = CutCornerShape(topEnd = 12.dp),
    large = CutCornerShape(topEnd = 16.dp),
    extraLarge = CutCornerShape(topEnd = 28.dp),
)

@Preview
@Composable
private fun ShapesPreview() {
    NeonTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            val shapes = listOf(
                "extraSmall" to MaterialTheme.shapes.extraSmall,
                "small" to MaterialTheme.shapes.small,
                "medium" to MaterialTheme.shapes.medium,
                "large" to MaterialTheme.shapes.large,
                "extraLarge" to MaterialTheme.shapes.extraLarge,
            )

            for ((label, shape) in shapes) {
                Surface(
                    shape = shape,
                    color = MaterialTheme.colorScheme.primary,
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        }
    }
}