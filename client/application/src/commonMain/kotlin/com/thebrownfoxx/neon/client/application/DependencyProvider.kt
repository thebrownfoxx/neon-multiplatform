package com.thebrownfoxx.neon.client.application

import androidx.lifecycle.viewmodel.CreationExtras
import com.thebrownfoxx.neon.client.application.http.HttpClient
import com.thebrownfoxx.neon.client.service.default.DefaultDependencies

object DependencyProvider {
    val dependencies = DefaultDependencies(HttpClient())
}

@Suppress("UnusedReceiverParameter")
val CreationExtras.dependencies get() = DependencyProvider.dependencies