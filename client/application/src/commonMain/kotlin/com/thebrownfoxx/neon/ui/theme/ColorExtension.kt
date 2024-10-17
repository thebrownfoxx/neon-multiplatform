package com.thebrownfoxx.neon.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.compositeOver

val ColorScheme.disabledOnSurface
    @Composable
    get() = onSurface.copy(alpha = 0.04f).compositeOver(surface)