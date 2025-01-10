package com.thebrownfoxx.neon.server.application.environment

import com.thebrownfoxx.neon.common.environment.EnvironmentKey

object ServerEnvironment {
    val Port = EnvironmentKey("PORT")
    val JwtSecret = EnvironmentKey("JWT_SECRET")
    val PostgresPassword = EnvironmentKey("POSTGRES_PASSWORD")
    val BasicAuthUsername = EnvironmentKey("BASIC_AUTH_USERNAME")
    val BasicAuthPassword = EnvironmentKey("BASIC_AUTH_PASSWORD")
}