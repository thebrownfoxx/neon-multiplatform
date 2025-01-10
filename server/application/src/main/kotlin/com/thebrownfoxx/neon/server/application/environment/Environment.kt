package com.thebrownfoxx.neon.server.application.environment

interface Environment {
    operator fun get(key: EnvironmentKey): String
}