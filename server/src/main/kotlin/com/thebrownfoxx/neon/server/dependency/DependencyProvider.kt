package com.thebrownfoxx.neon.server.dependency

object DependencyProvider {
    lateinit var dependencies: Dependencies

    fun init(dependencies: Dependencies) {
        DependencyProvider.dependencies = dependencies
    }
}
