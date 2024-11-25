package com.thebrownfoxx.neon.client.application

import android.app.Application
import com.thebrownfoxx.neon.client.service.Dependencies

class NeonApplication : Application() {
    private lateinit var _dependencies: Dependencies
    val dependencies get() = _dependencies

    override fun onCreate() {
        super.onCreate()
        _dependencies = createDependencies()
    }
}