package com.thebrownfoxx.neon.ui.component.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandIn
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntSize

enum class ExpandAxis {
    Horizontal,
    Vertical,
    Both,
}

// TODO: Fix clipping
@Composable
fun ThemedAnimatedVisibility(
    visible: Boolean,
    modifier: Modifier = Modifier,
    expandAxis: ExpandAxis = ExpandAxis.Both,
    label: String = "AnimatedVisibility",
    scale: Boolean = true,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    val expandSpring = spring(
        stiffness = Spring.StiffnessMedium,
        visibilityThreshold = IntSize.VisibilityThreshold,
    )
    val expandTransition = when (expandAxis) {
        ExpandAxis.Horizontal -> expandHorizontally(expandSpring)
        ExpandAxis.Vertical -> expandVertically(expandSpring)
        ExpandAxis.Both -> expandIn(expandSpring)
    }

    val shrinkSpring = spring(
        stiffness = Spring.StiffnessLow,
        visibilityThreshold = IntSize.VisibilityThreshold,
    )
    val shrinkTransition = when (expandAxis) {
        ExpandAxis.Horizontal -> shrinkHorizontally(shrinkSpring)
        ExpandAxis.Vertical -> shrinkVertically(shrinkSpring)
        ExpandAxis.Both -> shrinkOut(shrinkSpring)
    }

    var enterTransition = fadeIn(spring(stiffness = Spring.StiffnessHigh)) + expandTransition
    var exitTransition = fadeOut(spring(stiffness = Spring.StiffnessHigh)) + shrinkTransition
    if (scale) {
        enterTransition = scaleIn() + enterTransition
        exitTransition = scaleOut() + exitTransition
    }

    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = enterTransition,
        exit =  exitTransition,
        label = label,
        content = content,
    )
}

@Suppress("UnusedReceiverParameter")
@Composable
fun ColumnScope.ThemedAnimatedVisibility(
    visible: Boolean,
    modifier: Modifier = Modifier,
    expandAxis: ExpandAxis = ExpandAxis.Vertical,
    label: String = "AnimatedVisibility",
    scale: Boolean = true,
    content: @Composable AnimatedVisibilityScope.() -> Unit,
) {
    com.thebrownfoxx.neon.ui.component.common.ThemedAnimatedVisibility(
        visible = visible,
        modifier = modifier,
        expandAxis = expandAxis,
        label = label,
        scale = scale,
        content = content,
    )
}

@Suppress("UnusedReceiverParameter")
@Composable
fun RowScope.ThemedAnimatedVisibility(
    visible: Boolean,
    modifier: Modifier = Modifier,
    expandAxis: ExpandAxis = ExpandAxis.Horizontal,
    label: String = "AnimatedVisibility",
    scale: Boolean = true,
    content: @Composable AnimatedVisibilityScope.() -> Unit,
) {
    com.thebrownfoxx.neon.ui.component.common.ThemedAnimatedVisibility(
        visible = visible,
        modifier = modifier,
        expandAxis = expandAxis,
        label = label,
        scale = scale,
        content = content,
    )
}