@file:OptIn(InternalComponentApi::class, InternalComponentApi::class)

package com.thebrownfoxx.neon.client.application.ui.component.common.textfield

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Person
import androidx.compose.material.icons.twotone.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import com.thebrownfoxx.neon.client.application.ui.component.InternalComponentApi
import com.thebrownfoxx.neon.client.application.ui.theme.NeonTheme

@Preview
@Composable
private fun InteractivePreview() {
    val focusManager = LocalFocusManager.current

    NeonTheme {
        Box(
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { focusManager.clearFocus() }
        ) {
            var value by remember { mutableStateOf("") }

            TextField(
                label = "Label",
                value = value,
                onValueChange = { value = it },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview
@Composable
private fun PlainEmptyPreview() {
    NeonTheme {
        TextField(
            value = "",
            onValueChange = {},
        )
    }
}

@Preview
@Composable
private fun LabeledPreview() {
    NeonTheme {
        TextField(
            label = "Label",
            value = "",
            onValueChange = {},
        )
    }
}

@Preview
@Composable
private fun LabeledWithContentPreview() {
    NeonTheme {
        TextField(
            label = "Label",
            value = "Content",
            onValueChange = {},
        )
    }
}

@Preview
@Composable
private fun WithPlaceholderPreview() {
    NeonTheme {
        TextField(
            placeholder = "Placeholder",
            value = "",
            onValueChange = {},
        )
    }
}

@Preview
@Composable
private fun WithLeadingIconPreview() {
    NeonTheme {
        TextField(
            label = "Username",
            leadingIcon = {
                TextFieldIcon {
                    Icon(imageVector = Icons.TwoTone.Person, contentDescription = null)
                }
            },
            value = "",
            onValueChange = {},
        )
    }
}

@Preview
@Composable
private fun WithTrailingIconPreview() {
    NeonTheme {
        TextField(
            label = "Password",
            trailingIcon = {
                TextFieldIcon {
                    IconButton(onClick = {}) {
                        Icon(imageVector = Icons.TwoTone.VisibilityOff, contentDescription = null)
                    }
                }
            },
            value = "u3egfuyuygfuyegfyuegfgegfegfgeuigfeygfigyegfegfierigferufdfhuwiehfeuf",
            onValueChange = {},
        )
    }
}