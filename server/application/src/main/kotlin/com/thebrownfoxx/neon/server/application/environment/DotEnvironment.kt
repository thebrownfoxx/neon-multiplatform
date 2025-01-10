package com.thebrownfoxx.neon.server.application.environment

import io.github.cdimascio.dotenv.dotenv

class DotEnvironment : ServerEnvironment {
    private val dotenv = dotenv()

    override fun get(key: ServerEnvironmentKey): String {
        return dotenv[key.label]
    }
}