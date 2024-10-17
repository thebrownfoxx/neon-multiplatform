package com.thebrownfoxx.neon.ui.component.common.textfield

import androidx.compose.ui.graphics.Color

data class ThemedTextFieldStateColors(
    val unfocused: ThemedTextFieldColors,
    val focused: ThemedTextFieldColors,
    val disabled: ThemedTextFieldColors,
    val error: ThemedTextFieldColors,
) {
    fun getStateColors(focused: Boolean, enabled: Boolean, isError: Boolean) = when {
        isError -> this.error
        focused -> this.focused
        !enabled -> this.disabled
        else -> this.unfocused
    }
}

data class ThemedTextFieldColors(
    val containerColor: Color,
    val accentColor: Color,
    val contentColor: Color,
)