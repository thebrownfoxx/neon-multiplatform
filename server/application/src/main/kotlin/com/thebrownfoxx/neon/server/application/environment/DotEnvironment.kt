package com.thebrownfoxx.neon.server.application.environment

import com.thebrownfoxx.neon.common.environment.Environment
import com.thebrownfoxx.neon.common.environment.EnvironmentKey
import io.github.cdimascio.dotenv.dotenv

class DotEnvironment : Environment {
    private val dotenv = dotenv()

    override fun get(key: EnvironmentKey): String {
        return dotenv[key.name]
    }
}