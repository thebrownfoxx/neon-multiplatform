package com.thebrownfoxx.neon.server.application.dependency

import com.thebrownfoxx.neon.server.application.websocket.WebSocketManager
import com.thebrownfoxx.neon.server.service.authenticator.Authenticator
import com.thebrownfoxx.neon.server.service.group.GroupManager
import com.thebrownfoxx.neon.server.service.jwt.JwtProcessor
import com.thebrownfoxx.neon.server.service.member.MemberManager

interface Dependencies {
    val webSocketManager: WebSocketManager
    val jwtProcessor: JwtProcessor
    val authenticator: Authenticator
    val groupManager: GroupManager
    val memberManager: MemberManager
}
