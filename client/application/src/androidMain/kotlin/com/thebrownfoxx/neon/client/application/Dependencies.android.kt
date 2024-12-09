package com.thebrownfoxx.neon.client.application

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.thebrownfoxx.neon.client.service.Dependencies

val CreationExtras.application
    get() = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as NeonApplication)

actual val CreationExtras.dependencies: Dependencies
    get() = application.dependencies