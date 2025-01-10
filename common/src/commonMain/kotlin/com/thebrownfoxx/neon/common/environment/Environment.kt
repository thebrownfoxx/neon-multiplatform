package com.thebrownfoxx.neon.common.environment

interface Environment {
    operator fun get(key: EnvironmentKey): String
}

@JvmInline
value class EnvironmentKey(val name: String)