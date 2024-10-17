package com.thebrownfoxx.neon.client.application.ui.extension

import androidx.compose.ui.Modifier

fun Modifier.onlyIf(condition: Boolean, modifier: Modifier.() -> Modifier) = if (condition) {
    modifier()
} else {
    this
}

fun <T : Any> Modifier.onlyIfNotNull(value: T?, modifier: Modifier.(T) -> Modifier) =
    if (value != null) {
        modifier(value)
    } else {
        this
    }