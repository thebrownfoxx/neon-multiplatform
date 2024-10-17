package com.thebrownfoxx.neon.ui.component.common.textfield

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import com.thebrownfoxx.neon.ui.component.AppBarDefaults
import com.thebrownfoxx.neon.ui.theme.disabledOnSurface

object ThemedTextFieldDefaults {
    val Colors: ThemedTextFieldStateColors
        @Composable
        get() = ThemedTextFieldStateColors(
            unfocused = ThemedTextFieldColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                accentColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            focused = ThemedTextFieldColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                accentColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
            disabled = ThemedTextFieldColors(
                containerColor = MaterialTheme.colorScheme.disabledOnSurface,
                accentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            ),
            error = ThemedTextFieldColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                accentColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
            ),
        )

    val AppBarColors: ThemedTextFieldStateColors
        @Composable
        get() {
            val unfocused = ThemedTextFieldColors(
                containerColor = AppBarDefaults.ContainerColor,
                contentColor = contentColorFor(AppBarDefaults.ContainerColor),
                accentColor = MaterialTheme.colorScheme.primary,
            )

            return Colors.copy(unfocused = unfocused, focused = unfocused)
        }
}