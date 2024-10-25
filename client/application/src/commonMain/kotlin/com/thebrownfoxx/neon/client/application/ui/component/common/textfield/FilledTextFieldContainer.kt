package com.thebrownfoxx.neon.client.application.ui.component.common.textfield

import androidx.compose.animation.animateColorAsState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Shape
import com.thebrownfoxx.neon.client.application.ui.component.InternalComponentApi

@InternalComponentApi
@Composable
fun FilledTextFieldContainer(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    shape: Shape = MaterialTheme.shapes.small,
    colors: ThemedTextFieldStateColors = TextFieldDefaults.ThemedColors,
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
        content()
    }
}