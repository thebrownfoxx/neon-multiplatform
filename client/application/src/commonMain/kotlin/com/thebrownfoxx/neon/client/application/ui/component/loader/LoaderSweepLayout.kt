package com.thebrownfoxx.neon.client.application.ui.component.loader

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import com.thebrownfoxx.neon.common.type.Uuid
import com.thebrownfoxx.neon.client.application.ui.extension.rememberMutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@Composable
fun LoaderSweepLayout(
    modifier: Modifier = Modifier,
    sweepInterval: Duration = 500.milliseconds,
    sweepDuration: Duration = 5.seconds,
    content: @Composable LoaderSweepScope.() -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()

    val scope = remember { LoaderSweepScope(coroutineScope) }

    var maxOffset: IntOffset? by rememberMutableStateOf(null)

    val animationSpec = tween<Float>(
        delayMillis = sweepInterval.inWholeMilliseconds.toInt(),
        durationMillis = sweepDuration.inWholeMilliseconds.toInt(),
        easing = FastOutSlowInEasing,
    )

    val hasSubscribers = scope.hasSubscribers
    val pulseKey = scope.pulseKey

    LaunchedEffect(hasSubscribers, pulseKey) {
        val currentMaxOffset = maxOffset
        if (hasSubscribers && currentMaxOffset != null) {
            scope.animateSweepOffset(
                currentMaxOffset = currentMaxOffset,
                animationSpec = animationSpec,
            )
            scope.resetAnimation()
        } else {
            scope.resetAnimation()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { maxOffset = IntOffset(x = it.width, y = it.height) },
    ) {
        CompositionLocalProvider(LocalLoaderSweepScope provides scope) {
            scope.content()
        }
    }
}

class LoaderSweepScope(private val coroutineScope: CoroutineScope) {
    private val xOffset = Animatable(0f)
    private val yOffset = Animatable(0f)

    val offset get() = Offset(x = xOffset.value, y = yOffset.value)
    var pulseKey by mutableStateOf(Uuid())
        private set

    private val subscribers = mutableSetOf<LoaderScope>()
    var hasSubscribers by mutableStateOf(false)
        private set

    fun subscribe(loaderScope: LoaderScope) {
        subscribers.add(loaderScope)
        hasSubscribers = subscribers.isNotEmpty()
    }

    fun unsubscribe(loaderScope: LoaderScope) {
        subscribers.remove(loaderScope)
        hasSubscribers = subscribers.isNotEmpty()
    }

    suspend fun animateSweepOffset(
        currentMaxOffset: IntOffset,
        animationSpec: TweenSpec<Float>,
    ) {
        val xResult = coroutineScope.async {
            xOffset.animateTo(
                targetValue = currentMaxOffset.x.toFloat(),
                animationSpec = animationSpec,
            )
        }

        val yResult = coroutineScope.async {
            yOffset.animateTo(
                targetValue = currentMaxOffset.y.toFloat(),
                animationSpec = animationSpec,
            )
        }

        xResult.await()
        yResult.await()
        resetAnimation()
    }

    suspend fun resetAnimation() {
        xOffset.snapTo(0f)
        yOffset.snapTo(0f)
        resetKey()
    }

    private fun resetKey() {
        pulseKey = Uuid()
    }
}

val LocalLoaderSweepScope = compositionLocalOf<LoaderSweepScope?> { null }