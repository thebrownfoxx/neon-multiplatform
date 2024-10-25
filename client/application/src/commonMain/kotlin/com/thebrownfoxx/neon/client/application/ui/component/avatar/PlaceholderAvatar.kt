package com.thebrownfoxx.neon.client.application.ui.component.avatar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.thebrownfoxx.neon.client.application.ui.extension.isLight
import kotlin.random.Random

@Composable
private fun PlaceholderAvatar(
    placeholder: String?,
    size: AvatarSize,
    modifier: Modifier = Modifier,
) {
    val isLight = MaterialTheme.colorScheme.isLight

    val initial = remember(placeholder) { placeholder?.first()?.toString()?.uppercase() ?: "?" }
    val hue = remember(placeholder) {
        val seed = placeholder?.hashCode()
        seed?.let {
            Random(seed).nextFloat() * 360
        }
    }

    val containerColor = hue?.let {
        Color.hsl(
            hue = hue,
            saturation = 1f,
            lightness = if (isLight) 0.8f else 0.3f,
            alpha = 0.4f,
        )
    } ?: MaterialTheme.colorScheme.surfaceContainer

    val contentColor = hue?.let {
        Color.hsl(
            hue = hue,
            saturation = 1f,
            lightness = if (isLight) 0.4f else 0.8f,
            alpha = 0.8f,
        )
    } ?: MaterialTheme.colorScheme.secondary

    val textStyle = when (size) {
        AvatarSize.Small -> MaterialTheme.typography.labelSmall
        AvatarSize.Medium -> MaterialTheme.typography.titleSmall
        AvatarSize.Large -> MaterialTheme.typography.titleLarge
    }

    Surface(
        modifier = modifier
            .size(size.dp),
        color = containerColor,
        contentColor = contentColor,
        shape = CircleShape,
    ) {
        Box {
            val noFontScalingDensity = Density(
                density = LocalDensity.current.density,
                fontScale = 1f,
            )

            CompositionLocalProvider(LocalDensity provides noFontScalingDensity) {
                Text(
                    text = initial,
                    style = textStyle,
                    modifier = Modifier
                        .padding()
                        .align(Alignment.Center),
                )
            }
        }
    }
}

@Composable
fun LargePlaceholderAvatar(
    placeholder: String?,
    modifier: Modifier = Modifier,
) {
    PlaceholderAvatar(
        placeholder = placeholder,
        size = AvatarSize.Large,
        modifier = modifier,
    )
}

@Composable
fun MediumPlaceholderAvatar(
    placeholder: String?,
    modifier: Modifier = Modifier,
) {
    PlaceholderAvatar(
        placeholder = placeholder,
        size = AvatarSize.Medium,
        modifier = modifier,
    )
}

@Composable
fun SmallPlaceholderAvatar(
    placeholder: String?,
    modifier: Modifier = Modifier,
) {
    PlaceholderAvatar(
        placeholder = placeholder,
        size = AvatarSize.Small,
        modifier = modifier,
    )
}