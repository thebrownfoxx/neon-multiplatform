package com.thebrownfoxx.neon.client.application.ui.component.common.textfield

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import com.thebrownfoxx.neon.client.application.ui.component.InternalComponentApi

@OptIn(InternalComponentApi::class)
@Composable
fun FilledPasswordField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    visibilityHandler: PasswordFieldVisibilityHandler =
        PasswordFieldDefaults.PasswordVisibilityHandler,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    enabled: Boolean = true,
    isError: Boolean = false,
    shape: Shape = MaterialTheme.shapes.small,
    colors: ThemedTextFieldStateColors = TextFieldDefaults.ThemedColors,
) {
    FilledTextFieldContainer(
        modifier = modifier,
        enabled = enabled,
        isError = isError,
        shape = shape,
        colors = colors,
    ) {
        PasswordField(
            label = label,
            value = value,
            onValueChange = onValueChange,
            visibilityHandler = visibilityHandler,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
        )
    }
}