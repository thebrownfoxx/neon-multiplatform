package com.thebrownfoxx.neon.client.application.ui.component.common.textfield

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf

val LocalThemedTextFieldColors = compositionLocalOf<ThemedTextFieldStateColors?> { null }

@Suppress("UnusedReceiverParameter")
val ProvidableCompositionLocal<ThemedTextFieldStateColors?>.currentOrDefault
    @Composable
    get() = LocalThemedTextFieldColors.current ?: ThemedTextFieldDefaults.Colors

val LocalThemedTextFieldFocused = compositionLocalOf { false }

val LocalThemedTextFieldEnabled = compositionLocalOf { true }

val LocalThemedTextFieldIsError = compositionLocalOf { false }