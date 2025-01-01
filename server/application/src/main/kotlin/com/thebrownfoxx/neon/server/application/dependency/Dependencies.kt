package com.thebrownfoxx.neon.server.application.dependency

import com.thebrownfoxx.neon.common.Logger
import com.thebrownfoxx.neon.server.application.websocket.WebSocketManager
import com.thebrownfoxx.neon.server.service.Authenticator
import com.thebrownfoxx.neon.server.service.GroupManager
import com.thebrownfoxx.neon.server.service.JwtProcessor
import com.thebrownfoxx.neon.server.service.MemberManager
import com.thebrownfoxx.neon.server.service.Messenger
import kotlinx.coroutines.CoroutineScope

interface Dependencies {
    val applicationScope: CoroutineScope
    val webSocketManager: WebSocketManager
    val jwtProcessor: JwtProcessor
    val authenticator: Authenticator
    val groupManager: GroupManager
    val memberManager: MemberManager
    val messenger: Messenger
    val logger: Logger
}
