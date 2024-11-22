package com.thebrownfoxx.neon.client.application.ui.component.common.textfield

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Man
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.thebrownfoxx.neon.client.application.ui.theme.NeonTheme


@Preview
@Composable
private fun LeadingPreview() {
    NeonTheme {
        TextFieldIcon {
            Icon(imageVector = Icons.TwoTone.Man, contentDescription = null)
        }
    }
}

@Preview
@Composable
private fun TrailingPreview() {
    NeonTheme {
        TextFieldIcon {
            Icon(imageVector = Icons.TwoTone.Man, contentDescription = null)
        }
    }
}