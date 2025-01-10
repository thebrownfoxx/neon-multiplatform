package com.thebrownfoxx.neon.server.application.environment

import com.thebrownfoxx.neon.common.environment.Environment
import com.thebrownfoxx.neon.common.environment.EnvironmentKey

typealias ServerEnvironment = Environment<ServerEnvironmentKey>

enum class ServerEnvironmentKey(override val label: String) : EnvironmentKey {
    Port("PORT"),
    JwtSecret("JWT_SECRET"),
    PostgresPassword("POSTGRES_PASSWORD"),
    BasicAuthUsername("BASIC_AUTH_USERNAME"),
    BasicAuthPassword("BASIC_AUTH_PASSWORD"),
}