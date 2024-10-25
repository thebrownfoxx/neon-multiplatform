package com.thebrownfoxx.neon.client.application.ui.component.common

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.AttachMoney
import androidx.compose.material.icons.twotone.Colorize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.thebrownfoxx.neon.client.application.ui.theme.NeonTheme


@Preview
@Composable
private fun TextOnlyPreview() {
    NeonTheme {
        Button(onClick = {}, modifier = Modifier.padding(16.dp)) {
            Text(text = "Skibidi")
        }
    }
}

@Preview
@Composable
private fun StrongColorsIconPreview() {
    NeonTheme {
        Button(
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
        Button(
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