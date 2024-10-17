package com.thebrownfoxx.neon.client.application.ui.extension

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun <T> rememberCache(
    target: T,
    predicate: (T) -> Boolean = { it != null },
): T? {
    var cachedTarget by rememberMutableStateOf(target)

    LaunchedEffect(target) {
        if (predicate(target)) {
            cachedTarget = target
        }
    }

    return cachedTarget
}

@Composable
fun <T, R> rememberMappedCache(
    target: T,
    key: (T) -> R?,
): R? {
    var cachedMappedTarget by rememberMutableStateOf<R?>(null)

    LaunchedEffect(target) {
        val mappedValue = key(target)
        if (mappedValue != null) {
            cachedMappedTarget = mappedValue
        }
    }

    return cachedMappedTarget
}