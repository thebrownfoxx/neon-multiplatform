package com.thebrownfoxx.neon.client.application.ui.component.common.textfield

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.thebrownfoxx.neon.client.application.ui.component.InternalComponentApi
import com.thebrownfoxx.neon.client.application.ui.theme.NeonTheme

@OptIn(InternalComponentApi::class)
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

            FilledTextFieldContainer(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
            ) {
                TextField(
                    label = "Label",
                    value = value,
                    onValueChange = { value = it },
                )
            }
        }
    }
}