package com.thebrownfoxx.neon.ui.extension

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.luminance

val ColorScheme.isLight @Composable get() = this.background.luminance() > 0.5