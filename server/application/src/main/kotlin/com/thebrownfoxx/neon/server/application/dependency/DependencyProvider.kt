package com.thebrownfoxx.neon.server.application.dependency

object DependencyProvider {
    lateinit var dependencies: Dependencies

    fun init(dependencies: Dependencies) {
        DependencyProvider.dependencies = dependencies
    }
}
