package com.thebrownfoxx.neon.client.application.ui.extension

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
actual fun calculateWindowSizeClass(): WindowSizeClass? {
    val activity = LocalContext.current.getActivityOrNull()
    return activity?.let { calculateWindowSizeClass(it) }
}

private fun Context.getActivityOrNull(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
internal val CompactWindowSizeClass
    get() = WindowSizeClass.calculateFromSize(DpSize(width = 400.dp, height = 800.dp))

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
internal val MediumWindowSizeClass
    get() = WindowSizeClass.calculateFromSize(DpSize(width = 700.dp, height = 600.dp))

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
internal val ExpandedWindowSizeClass
    get() = WindowSizeClass.calculateFromSize(DpSize(width = 1000.dp, height = 800.dp))