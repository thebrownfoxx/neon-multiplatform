package com.thebrownfoxx.neon.client.application.ui.extension

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf

@Composable
expect fun calculateWindowSizeClass(): WindowSizeClass?

val LocalWindowSizeClass = compositionLocalOf<WindowSizeClass?> { null }