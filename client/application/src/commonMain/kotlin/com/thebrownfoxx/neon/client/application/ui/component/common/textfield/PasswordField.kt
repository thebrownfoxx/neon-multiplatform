package com.thebrownfoxx.neon.client.application.ui.component.common.textfield

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Visibility
import androidx.compose.material.icons.twotone.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.thebrownfoxx.neon.client.application.ui.component.InternalComponentApi
import com.thebrownfoxx.neon.client.application.ui.extension.rememberMutableStateOf

@InternalComponentApi
@Composable
fun PasswordField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    visibilityHandler: PasswordFieldVisibilityHandler =
        PasswordFieldDefaults.PasswordVisibilityHandler,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    var focused by rememberMutableStateOf(false)

    with(visibilityHandler) {
        val visualTransformation = when {
            passwordVisible -> VisualTransformation.None
            else -> PasswordVisualTransformation()
        }

        TextField(
            value = value,
            onValueChange = onValueChange,
            label = label,
            modifier = modifier.onFocusChanged { focused = it.hasFocus },
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            trailingIcon = {
                VisibilityToggle(
                    passwordVisible = passwordVisible,
                    onPasswordVisibleToggle = onPasswordVisibleToggle,
                    focused = focused,
                    value = value,
                )
            }
        )
    }
}

@Composable
private fun VisibilityToggle(
    passwordVisible: Boolean,
    onPasswordVisibleToggle: () -> Unit,
    focused: Boolean,
    value: String
) {
    val icon = when {
        passwordVisible -> Icons.TwoTone.Visibility
        else -> Icons.TwoTone.VisibilityOff
    }

    ThemedTextFieldTrailingIcon(visible = focused || value.isNotEmpty()) {
        IconButton(onClick = onPasswordVisibleToggle) {
            AnimatedContent(targetState = icon, label = "icon") { targetIcon ->
                Icon(
                    imageVector = targetIcon,
                    contentDescription = null,
                )
            }
        }
    }
}

class PasswordFieldVisibilityHandler(
    val passwordVisible: Boolean,
    val onPasswordVisibleToggle: () -> Unit,
)

object PasswordFieldDefaults {
    val PasswordVisibilityHandler: PasswordFieldVisibilityHandler
        @Composable
        get() {
            var passwordVisible by rememberMutableStateOf(false)

            return PasswordFieldVisibilityHandler(
                passwordVisible = passwordVisible,
                onPasswordVisibleToggle = { passwordVisible = !passwordVisible },
            )
        }
}