package com.thebrownfoxx.neon.server

import com.thebrownfoxx.neon.server.dependency.DefaultDependencies
import com.thebrownfoxx.neon.server.dependency.DependencyProvider
import io.ktor.server.application.Application

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    DependencyProvider.init(DefaultDependencies())
    configureRouting()
}