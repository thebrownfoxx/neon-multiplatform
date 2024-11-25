package com.thebrownfoxx.neon.client.application

import androidx.lifecycle.viewmodel.CreationExtras
import com.thebrownfoxx.neon.client.service.Dependencies

object DependencyProvider {
    val dependencies = createDependencies()
}

actual val CreationExtras.dependencies: Dependencies get() = DependencyProvider.dependencies