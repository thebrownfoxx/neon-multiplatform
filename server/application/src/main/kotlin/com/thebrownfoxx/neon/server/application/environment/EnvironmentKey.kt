package com.thebrownfoxx.neon.server.application.environment

enum class EnvironmentKey(val label: String) {
    Port("PORT"),
    JwtSecret("JWT_SECRET"),
    PostgresPassword("POSTGRES_PASSWORD"),
    BasicAuthUsername("BASIC_AUTH_USERNAME"),
    BasicAuthPassword("BASIC_AUTH_PASSWORD"),
}