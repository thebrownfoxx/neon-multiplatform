package com.thebrownfoxx.neon.client.application.ui.component.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.AttachMoney
import androidx.compose.material.icons.twotone.Colorize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.thebrownfoxx.neon.client.application.ui.extension.RoundedCorners
import com.thebrownfoxx.neon.client.application.ui.theme.NeonTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

object ThemedButtonDefaults {
    val strongColors
        @Composable get() = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.tertiary,
            contentColor = MaterialTheme.colorScheme.onTertiary,
        )

    val weakColors
        @Composable get() = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        )

    val contentPadding
        @Composable get() = PaddingValues(
            start = 32.dp,
            top = 8.dp,
            end = 32.dp,
            bottom = 8.dp
        )
}

@Composable
fun ButtonIconText(
    icon: ImageVector,
    iconContentDescription: String?,
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = iconContentDescription,
        )
        Spacer(modifier = Modifier.width(width = 8.dp))
        Text(text = text)
    }
}

@Composable
fun ThemedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    roundedCorners: RoundedCorners = RoundedCorners.All,
    colors: ButtonColors = ThemedButtonDefaults.strongColors,
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ThemedButtonDefaults.contentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable RowScope.() -> Unit,
) {
    val circleRadius = 100.dp

    val shape = roundedCorners.toShape(roundRadius = circleRadius)

    Button(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = 48.dp),
        enabled = enabled,
        shape = shape,
        colors = colors,
        elevation = elevation,
        border = border,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        content = content,
    )
}

@Preview
@Composable
private fun TextOnlyPreview() {
    NeonTheme {
        ThemedButton(onClick = {}, modifier = Modifier.padding(16.dp)) {
            Text(text = "Skibidi")
        }
    }
}

@Preview
@Composable
private fun StrongColorsIconPreview() {
    NeonTheme {
        ThemedButton(
            onClick = {},
            modifier = Modifier.padding(16.dp),
        ) {
            ButtonIconText(
                icon = Icons.TwoTone.Colorize,
                iconContentDescription = null,
                text = "Rizz",
            )
        }
    }
}

@Preview
@Composable
private fun WeakColorsIconPreview() {
    NeonTheme {
        ThemedButton(
            onClick = {},
            modifier = Modifier.padding(16.dp),
            colors = ThemedButtonDefaults.weakColors,
        ) {
            ButtonIconText(
                icon = Icons.TwoTone.AttachMoney,
                iconContentDescription = null,
                text = "Collect fanum tax",
            )
        }
    }
}
