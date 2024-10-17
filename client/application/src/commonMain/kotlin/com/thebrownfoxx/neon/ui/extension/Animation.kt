package com.thebrownfoxx.neon.ui.extension

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp

fun sharedZAxisExit(reversed: Boolean = false) =
    scaleOut(
        animationSpec = tween(durationMillis = 300),
        targetScale = if (!reversed) 1.1f else 0.8f,
    ) + fadeOut(
        animationSpec = tween(durationMillis = 90),
    )

fun sharedZAxisEnter(reversed: Boolean = false) =
    scaleIn(
        animationSpec = tween(durationMillis = 300),
        initialScale = if (!reversed) 0.8f else 1.1f,
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = 210,
            delayMillis = 90,
        ),
    )

fun Density.sharedXAxisExit(reversed: Boolean = false) =
    slideOutHorizontally(
        animationSpec = tween(durationMillis = 300),
        targetOffsetX = { with(this) { (if (!reversed) -30 else 30).dp.roundToPx() } },
    ) + fadeOut(
        animationSpec = tween(durationMillis = 90),
    )

fun Density.sharedXAxisEnter(reversed: Boolean = false) =
    slideInHorizontally(
        animationSpec = tween(durationMillis = 300),
        initialOffsetX = { with(this) { (if (!reversed) 30 else -30).dp.roundToPx() } }
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = 210,
            delayMillis = 90,
        ),
    )

fun Density.sharedYAxisExit(reversed: Boolean = false) =
    slideOutVertically(
        animationSpec = tween(durationMillis = 300),
        targetOffsetY = { with(this) { (if (!reversed) -30 else 30).dp.roundToPx() } },
    ) + fadeOut(
        animationSpec = tween(durationMillis = 90),
    )

fun Density.sharedYAxisEnter(reversed: Boolean = false) =
    slideInVertically(
        animationSpec = tween(durationMillis = 300),
        initialOffsetY = { with(this) { (if (!reversed) 30 else -30).dp.roundToPx() } }
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = 210,
            delayMillis = 90,
        ),
    )

fun <S> crossfadeTransition(): AnimatedContentTransitionScope<S>.() -> ContentTransform = {
    ContentTransform(
        targetContentEnter = fadeIn(),
        initialContentExit = fadeOut()
    )
}

fun loaderContentTransition() = fadeIn(animationSpec = tween(durationMillis = 400)) togetherWith
        fadeOut(animationSpec = tween(durationMillis = 100, delayMillis = 100))