package com.thebrownfoxx.neon.common.environment

interface Environment<K : EnvironmentKey> {
    operator fun get(key: K): String
}

interface EnvironmentKey {
    val label: String
}