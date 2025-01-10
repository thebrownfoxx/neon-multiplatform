package com.thebrownfoxx.neon.server.application.environment

import io.github.cdimascio.dotenv.dotenv

class DotEnvironment : Environment {
    private val dotenv = dotenv()

    override fun get(key: EnvironmentKey): String {
        return dotenv[key.label]
    }
}