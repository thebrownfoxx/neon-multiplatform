package com.thebrownfoxx.neon.client.application.ui.component.common.textfield

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.thebrownfoxx.neon.client.application.ui.theme.NeonTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

// TODO: fix weird height change on focus change when text is empty and the
//  font size is larger than standard
@Composable
fun ThemedFilledTextFieldContainer(
    modifier: Modifier = Modifier,
    enabled: Boolean = LocalThemedTextFieldEnabled.current,
    isError: Boolean = LocalThemedTextFieldIsError.current,
    shape: Shape = MaterialTheme.shapes.small,
    colors: ThemedTextFieldStateColors = LocalThemedTextFieldColors.currentOrDefault,
    content: @Composable () -> Unit,
) {
    var focused by remember { mutableStateOf(false) }

    val stateColors = colors.getStateColors(
        focused = focused,
        enabled = enabled,
        isError = isError,
    )

    val containerColor by animateColorAsState(
        targetValue = stateColors.containerColor,
        label = "containerColor",
    )

    Surface(
        color = containerColor,
        shape = shape,
        modifier = modifier.onFocusChanged { focused = it.hasFocus },
    ) {
        CompositionLocalProvider(
            LocalThemedTextFieldColors provides colors,
            LocalThemedTextFieldFocused provides focused,
            LocalThemedTextFieldEnabled provides enabled,
            LocalThemedTextFieldIsError provides isError,
        ) {
            content()
        }
    }
}

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

            ThemedFilledTextFieldContainer(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
            ) {
                ThemedTextField(
                    label = "Label",
                    value = value,
                    onValueChange = { value = it },
                )
            }
        }
    }
}