package com.thebrownfoxx.neon.client.service

import com.thebrownfoxx.neon.common.Logger
import com.thebrownfoxx.outcome.Outcome

interface Dependencies {
    val logger: Logger
    val tokenStorage: TokenStorage
    val authenticator: Authenticator
    val groupManager: GroupManager
    val memberManager: MemberManager
    val messenger: Messenger

    @Deprecated("Use groupManager instead")
    suspend fun getGroupManager(): Outcome<GroupManager, GetGroupManagerError>

    enum class GetGroupManagerError {
        Unauthorized,
        ConnectionError,
    }
}