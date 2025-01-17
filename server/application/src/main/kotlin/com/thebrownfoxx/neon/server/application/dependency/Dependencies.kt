package com.thebrownfoxx.neon.server.application.dependency

import com.thebrownfoxx.neon.server.application.environment.ServerEnvironment
import com.thebrownfoxx.neon.server.application.websocket.WebSocketManager
import com.thebrownfoxx.neon.server.service.Authenticator
import com.thebrownfoxx.neon.server.service.GroupManager
import com.thebrownfoxx.neon.server.service.JwtProcessor
import com.thebrownfoxx.neon.server.service.MemberManager
import com.thebrownfoxx.neon.server.service.Messenger
import kotlinx.coroutines.CoroutineScope

interface Dependencies {
    val environment: ServerEnvironment
    val applicationScope: CoroutineScope
    val webSocketManager: WebSocketManager
    val jwtProcessor: JwtProcessor
    val authenticator: Authenticator
    val groupManager: GroupManager
    val memberManager: MemberManager
    val messenger: Messenger
}
