package com.thebrownfoxx.neon.client.application.environment

import com.thebrownfoxx.neon.common.environment.Environment
import com.thebrownfoxx.neon.common.environment.EnvironmentKey

typealias ClientEnvironment = Environment<ClientEnvironmentKey>

enum class ClientEnvironmentKey(override val label: String) : EnvironmentKey {
    Host("HOST"),
    Port("PORT"),
    LocalPath("LOCAL_PATH"),
}