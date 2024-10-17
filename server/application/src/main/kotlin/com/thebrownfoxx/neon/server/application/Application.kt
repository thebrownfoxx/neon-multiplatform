package com.thebrownfoxx.neon.server.application

import com.thebrownfoxx.neon.server.application.dependency.DefaultDependencies
import com.thebrownfoxx.neon.server.application.dependency.DependencyProvider
import com.thebrownfoxx.neon.server.application.plugin.configureSecurity
import com.thebrownfoxx.neon.server.application.routing.configureRouting
import io.ktor.server.application.Application

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    DependencyProvider.init(DefaultDependencies())
    configureSecurity()
    configureRouting()
}