package com.thebrownfoxx.neon.client.application.environment

import com.thebrownfoxx.neon.client.application.BuildKonfig
import com.thebrownfoxx.neon.common.environment.Environment

class BuildKonfigEnvironment : Environment<ClientEnvironmentKey> {
    override fun get(key: ClientEnvironmentKey): String {
        return when (key) {
            ClientEnvironmentKey.Host -> BuildKonfig.HOST
            ClientEnvironmentKey.Port -> BuildKonfig.PORT
            ClientEnvironmentKey.LocalPath -> BuildKonfig.LOCAL_PATH
        }
    }
}