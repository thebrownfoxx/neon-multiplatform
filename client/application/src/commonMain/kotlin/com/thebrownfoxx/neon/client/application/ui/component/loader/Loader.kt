package com.thebrownfoxx.neon.client.application.ui.component.loader

import androidx.compose.animation.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import com.thebrownfoxx.neon.client.application.ui.extension.rememberMutableStateOf
import com.thebrownfoxx.neon.common.type.id.Uuid
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun Loader(
    modifier: Modifier = Modifier,
    loaderSweepScope: LoaderSweepScope? = LocalLoaderSweepScope.current,
    content: @Composable LoaderScope.(loaderColor: Color) -> Unit,
) {
    val scope = remember { LoaderScope() }

    with(scope) {
        val loaderColor by rememberLoaderColorAsState()

        DisposableEffect(scope, loaderSweepScope) {
            loaderSweepScope?.subscribe(scope)
            onDispose {
                loaderSweepScope?.unsubscribe(scope)
            }
        }

        Box(modifier = modifier.onGloballyPositioned { offset = it.positionInRoot() }) {
            content(loaderColor)
        }
    }
}

@Composable
fun LoaderScope.rememberLoaderColorAsState(
    offset: Offset? = this.offset,
    color: Color = LocalContentColor.current,
    pulseWidth: Duration = 500.milliseconds,
    sweepScope: LoaderSweepScope? = LocalLoaderSweepScope.current,
): State<Color> {
    val minAlpha = 0.1f
    val maxAlpha = 0.2f
    val coroutineScope = rememberCoroutineScope()
    val pulsingColor = remember { Animatable(color.copy(alpha = minAlpha)) }
    var currentPulseKey by rememberMutableStateOf(Uuid())
    val sweepOffset = sweepScope?.offset
    val scopePulseKey = sweepScope?.pulseKey

    LaunchedEffect(offset, sweepOffset, scopePulseKey) {
        if (
            sweepScope != null &&
            sweepOffset != null &&
            scopePulseKey != null &&
            currentPulseKey != scopePulseKey &&
            offset != null &&
            sweepOffset.x >= offset.x &&
            sweepOffset.y >= offset.y
        ) {
            coroutineScope.launch {
                currentPulseKey = scopePulseKey
                pulsingColor.animateTo(
                    color.copy(alpha = maxAlpha),
                    animationSpec = tween(
                        durationMillis = pulseWidth.inWholeMilliseconds.toInt(),
                        easing = FastOutSlowInEasing,
                    ),
                )
                pulsingColor.animateTo(
                    color.copy(alpha = minAlpha),
                    animationSpec = tween(
                        durationMillis = pulseWidth.inWholeMilliseconds.toInt(),
                        easing = FastOutSlowInEasing,
                    ),
                )
            }
        }
    }

    return pulsingColor.asState()
}

class LoaderScope {
    val id = Uuid()
    var offset by mutableStateOf<Offset?>(null)
}
