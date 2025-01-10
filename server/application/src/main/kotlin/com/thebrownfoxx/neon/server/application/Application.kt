package com.thebrownfoxx.neon.server.application

import com.thebrownfoxx.neon.server.application.dependency.DefaultDependencies
import com.thebrownfoxx.neon.server.application.dependency.DependencyProvider
import com.thebrownfoxx.neon.server.application.environment.EnvironmentKey
import com.thebrownfoxx.neon.server.application.plugin.configureSecurity
import com.thebrownfoxx.neon.server.application.routing.configureRouting
import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main() {
    DependencyProvider.init(DefaultDependencies())
    val port = DependencyProvider.dependencies.environment[EnvironmentKey.Port].toInt()
    embeddedServer(Netty, port = port, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureSecurity()
    configureRouting()
}