package com.thebrownfoxx.neon.client.service

import com.thebrownfoxx.outcome.Outcome

interface Dependencies {
    val tokenStorage: TokenStorage
    val authenticator: Authenticator
    suspend fun getGroupManager(): Outcome<GroupManager, GetGroupManagerError>

    enum class GetGroupManagerError {
        Unauthorized,
        ConnectionError,
    }
}