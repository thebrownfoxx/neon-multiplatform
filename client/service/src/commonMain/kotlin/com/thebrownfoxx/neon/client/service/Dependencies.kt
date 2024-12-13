package com.thebrownfoxx.neon.client.service

import com.thebrownfoxx.neon.common.Logger

interface Dependencies {
    val logger: Logger
    val tokenStorage: TokenStorage
    val authenticator: Authenticator
    val groupManager: GroupManager
    val memberManager: MemberManager
    val messenger: Messenger

    enum class GetGroupManagerError {
        Unauthorized,
        ConnectionError,
    }
}