package com.thebrownfoxx.neon.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.thebrownfoxx.neon.ui.component.common.ThemedAnimatedVisibility
import com.thebrownfoxx.neon.ui.extension.RoundedCorners
import com.thebrownfoxx.neon.ui.theme.NeonTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun InformationCardProgressIndicator(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    ThemedAnimatedVisibility(
        visible = visible,
        scale = false,
        modifier = modifier,
    ) {
        content()
    }
}

@Composable
fun InformationCardIconText(
    icon: ImageVector,
    iconContentDescription: String?,
    text: String,
    modifier: Modifier = Modifier,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
) {
    Row(
        verticalAlignment = verticalAlignment,
        modifier = modifier,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = iconContentDescription,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun InformationCard(
    modifier: Modifier = Modifier,
    progressIndicator: @Composable BoxScope.() -> Unit = {},
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    contentColor: Color = contentColorFor(containerColor),
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        color = containerColor,
        contentColor = contentColor,
        shape = RoundedCorners.All.toShape(),
    ) {
        Box {
            Box(modifier = Modifier.padding(16.dp)) {
                ProvideTextStyle(value = MaterialTheme.typography.bodySmall) {
                    content()
                }
            }
            Box(modifier = Modifier.align(Alignment.BottomStart)) {
                progressIndicator()
            }
        }
    }
}

@Preview
@Composable
private fun NoProgressPreview() {
    NeonTheme {
        InformationCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            InformationCardIconText(
                icon = Icons.TwoTone.Info,
                iconContentDescription = null,
                text = "Did you know that skibidi wah pow pow",
            )
        }
    }
}

@Preview
@Composable
private fun ProgressPreview() {
    NeonTheme {
        InformationCard(
            progressIndicator = {
                LinearProgressIndicator(
                    progress = { 0.8f },
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(text = "Rizzmaxxing...")
        }
    }
}