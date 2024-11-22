package com.thebrownfoxx.neon.client.application.ui.component.common.textfield

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Close
import androidx.compose.material.icons.twotone.Person
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.thebrownfoxx.neon.client.application.ui.theme.NeonTheme

@Preview
@Composable
private fun Preview() {
    NeonTheme {
        FilledTextField(
            label = "Text",
            value = "Hello, World",
            onValueChange = {},
            leadingIcon = {
                TextFieldIcon {
                    Icon(
                        imageVector = Icons.TwoTone.Person,
                        contentDescription = null,
                    )
                }
            },
            trailingIcon = {
                TextFieldIcon {
                    Icon(
                        imageVector = Icons.TwoTone.Close,
                        contentDescription = null,
                    )
                }
            },
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview
@Composable
private fun DisabledPreview() {
    NeonTheme {
        FilledTextField(
            label = "Text",
            value = "Hello, World",
            onValueChange = {},
            leadingIcon = {
                TextFieldIcon {
                    Icon(
                        imageVector = Icons.TwoTone.Person,
                        contentDescription = null,
                    )
                }
            },
            trailingIcon = {
                TextFieldIcon {
                    Icon(
                        imageVector = Icons.TwoTone.Close,
                        contentDescription = null,
                    )
                }
            },
            enabled = false,
            modifier = Modifier.padding(16.dp),
        )
    }
}