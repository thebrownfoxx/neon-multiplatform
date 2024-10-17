package com.thebrownfoxx.neon.client.application.ui.component.common.textfield

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastMap
import androidx.compose.ui.util.fastMaxOfOrNull
import kotlin.math.max
import kotlin.math.min

@Composable
fun ThemedTextFieldLayout(
    leadingIcon: @Composable () -> Unit,
    trailingIcon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit,
) {
    SubcomposeLayout(modifier = modifier) { constraints ->
        val minHeight = 56.dp.roundToPx()
        val horizontalPadding = 16.dp.roundToPx()

        val leadingIconMeasurables =
            subcompose(ThemedTextFieldLayoutContent.LeadingIcon, leadingIcon)

        val leadingIconWidth = leadingIconMeasurables.fastMaxOfOrNull {
            it.maxIntrinsicWidth(minHeight)
        } ?: 0

        val paddedLeadingIconWidth = max(leadingIconWidth, horizontalPadding)

        val trailingIconMeasurables =
            subcompose(ThemedTextFieldLayoutContent.TrailingIcon, trailingIcon)

        val trailingIconWidth = trailingIconMeasurables.fastMaxOfOrNull {
            it.maxIntrinsicWidth(minHeight)
        } ?: 0

        val paddedTrailingIconWidth = max(trailingIconWidth, horizontalPadding)

        val contentPlaceables = subcompose(ThemedTextFieldLayoutContent.Content) {
            val innerPadding = PaddingValues(
                start = paddedLeadingIconWidth.toDp(),
                end = paddedTrailingIconWidth.toDp(),
            )
            content(innerPadding)
        }.map { it.measure(constraints) }

        val contentHeight = contentPlaceables.fastMaxOfOrNull { it.height } ?: 0

        val height = max(contentHeight, minHeight)

        val iconConstraints = constraints.copy(
            minWidth = horizontalPadding,
            minHeight = height,
            maxHeight = height,
        )

        val leadingIconPlaceables = leadingIconMeasurables.fastMap { it.measure(iconConstraints) }

        val trailingIconPlaceables = trailingIconMeasurables.fastMap { it.measure(iconConstraints) }

        val contentWidth = contentPlaceables.fastMaxOfOrNull { it.width } ?: 0

        val width = min(constraints.maxWidth, contentWidth)

        val actualTrailingIconWidth = trailingIconPlaceables.fastMaxOfOrNull { it.width } ?: 0

        layout(width, height) {
            contentPlaceables.fastMap { it.place(x = 0, y = height / 2 - contentHeight / 2) }
            leadingIconPlaceables.fastMap { it.place(x = 0, y = 0) }
            trailingIconPlaceables.fastMap { it.place(x = width - actualTrailingIconWidth, y = 0) }
        }
    }
}

enum class ThemedTextFieldLayoutContent {
    LeadingIcon,
    TrailingIcon,
    Content,
}