package com.thebrownfoxx.neon.client.application.ui.extension

import androidx.compose.ui.unit.dp

object Elevation {
    fun level(value: Int) = when (value) {
        1 -> 1.dp
        2 -> 3.dp
        3 -> 6.dp
        4 -> 8.dp
        5 -> 12.dp
        else -> 0.dp
    }
}