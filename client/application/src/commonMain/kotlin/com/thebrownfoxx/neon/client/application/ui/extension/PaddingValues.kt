package com.thebrownfoxx.neon.client.application.ui.extension

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val PaddingValues.left @Composable get() = calculateLeftPadding(LocalLayoutDirection.current)
val PaddingValues.right @Composable get() = calculateRightPadding(LocalLayoutDirection.current)

val PaddingValues.start @Composable get() = calculateStartPadding(LocalLayoutDirection.current)
val PaddingValues.top get() = calculateTopPadding()
val PaddingValues.end @Composable get() = calculateEndPadding(LocalLayoutDirection.current)
val PaddingValues.bottom get() = calculateBottomPadding()

val StatusBarPadding @Composable get() = WindowInsets.statusBars.asPaddingValues()
val NavigationBarPadding @Composable get() = WindowInsets.navigationBars.asPaddingValues()
val StatusBarHeight @Composable get() = StatusBarPadding.top
val NavigationBarHeight @Composable get() = NavigationBarPadding.bottom

val PaddingValues.horizontal @Composable get() = PaddingValues(
    start = start,
    end = end,
)

val PaddingValues.vertical get() = PaddingValues(
    top = top,
    bottom = bottom,
)

@Composable
operator fun PaddingValues.plus(other: PaddingValues) = PaddingValues(
    start = start + other.start,
    top = top + other.top,
    end = end + other.end,
    bottom = bottom + other.bottom,
)

@Composable
operator fun PaddingValues.minus(other: PaddingValues) = PaddingValues(
    start = start - other.start,
    top = top - other.top,
    end = end - other.end,
    bottom = bottom - other.bottom,
)

enum class PaddingSide {
    Start,
    Top,
    End,
    Bottom,
}

val Dp.padding @Composable get() = PaddingValues(all = this)

@Composable
fun Dp.paddingFor(vararg sides: PaddingSide): PaddingValues {
    var paddingValues = 0.dp.padding
    for (side in sides) {
        paddingValues += when (side) {
            PaddingSide.Start -> PaddingValues(start = this)
            PaddingSide.Top -> PaddingValues(top = this)
            PaddingSide.End -> PaddingValues(end = this)
            PaddingSide.Bottom -> PaddingValues(bottom = this)
        }
    }
    return paddingValues
}

val Dp.startPadding @Composable get() = PaddingValues(start = this)
val Dp.topPadding @Composable get() = PaddingValues(top = this)
val Dp.bottomPadding @Composable get() = PaddingValues(bottom = this)
val Dp.endPadding @Composable get() = PaddingValues(end = this)

val Dp.horizontalPadding @Composable get() = PaddingValues(horizontal = this)
val Dp.verticalPadding @Composable get() = PaddingValues(vertical = this)

@Composable
fun Dp.paddingExcept(vararg sides: PaddingSide): PaddingValues {
    var paddingValues = PaddingValues(all = this)
    for (side in sides) {
        paddingValues -= when (side) {
            PaddingSide.Start -> PaddingValues(start = this)
            PaddingSide.Top -> PaddingValues(top = this)
            PaddingSide.End -> PaddingValues(end = this)
            PaddingSide.Bottom -> PaddingValues(bottom = this)
        }
    }
    return paddingValues
}

@Composable
fun PaddingValues.copy(
    start: Dp = this.start,
    top: Dp = this.top,
    end: Dp = this.end,
    bottom: Dp = this.bottom,
) = PaddingValues(
    start = start,
    top = top,
    end = end,
    bottom = bottom,
)