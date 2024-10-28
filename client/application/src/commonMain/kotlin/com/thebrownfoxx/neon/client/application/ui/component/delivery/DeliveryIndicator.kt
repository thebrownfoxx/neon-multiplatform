package com.thebrownfoxx.neon.client.application.ui.component.delivery

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Check
import androidx.compose.material.icons.twotone.DoneAll
import androidx.compose.material.icons.twotone.Error
import androidx.compose.material.icons.twotone.Pending
import androidx.compose.material.icons.twotone.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thebrownfoxx.neon.client.application.ui.component.delivery.state.DeliveryState
import neon.client.application.generated.resources.Res
import neon.client.application.generated.resources.delivered
import neon.client.application.generated.resources.failed
import neon.client.application.generated.resources.read
import neon.client.application.generated.resources.sending
import neon.client.application.generated.resources.sent
import org.jetbrains.compose.resources.stringResource

@Composable
fun DeliveryIndicator(
    delivery: DeliveryState,
    modifier: Modifier = Modifier,
    clippingBackground: Color = MaterialTheme.colorScheme.surface,
) {
    Box(
        modifier = modifier
            .size(20.dp)
            .clip(CircleShape)
            .background(clippingBackground),
    ) {
        Box(modifier = Modifier
            .size(16.dp)
            .align(Alignment.Center)
        ) {
            var icon: ImageVector? = null
            var contentDescription: String? = null

            when (delivery) {
                is DeliveryState.Reacted -> ReactionEmoji(emoji = delivery.emoji)

                DeliveryState.Sending -> {
                    icon = Icons.TwoTone.Pending
                    contentDescription = stringResource(Res.string.sending)
                }

                DeliveryState.Sent -> {
                    icon = Icons.TwoTone.Check
                    contentDescription = stringResource(Res.string.sent)
                }

                DeliveryState.Delivered -> {
                    icon = Icons.TwoTone.DoneAll
                    contentDescription = stringResource(Res.string.delivered)
                }

                DeliveryState.Read -> {
                    icon = Icons.TwoTone.Visibility
                    contentDescription = stringResource(Res.string.read)
                }

                DeliveryState.Failed -> {
                    icon = Icons.TwoTone.Error
                    contentDescription = stringResource(Res.string.failed)
                }
            }

            if (icon != null) {
                val tint = when (delivery) {
                    is DeliveryState.Failed -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurface
                }

                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = tint,
                    modifier = Modifier,
                )
            }
        }
    }
}

@Composable
private fun ReactionEmoji(
    emoji: String,
    modifier: Modifier = Modifier,
) {
    val density = Density(LocalDensity.current.density, fontScale = 1f)
    CompositionLocalProvider(LocalDensity provides density) {
        Box(modifier = modifier) {
            val textMeasurer = rememberTextMeasurer()

            val style = TextStyle(fontSize = 14.sp)

            val textLayoutResult = remember(emoji) {
                textMeasurer.measure(text = emoji, style = style)
            }

            Canvas(modifier = Modifier.size(16.dp)) {
                drawText(
                    textMeasurer = textMeasurer,
                    text = emoji,
                    style = style,
                    topLeft = Offset(
                        x = center.x - textLayoutResult.size.width / 2,
                        y = center.y - textLayoutResult.size.height / 2,
                    )
                )
            }
        }
    }
}