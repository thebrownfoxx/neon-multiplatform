package com.thebrownfoxx.neon.server.dependency

import com.thebrownfoxx.neon.server.service.authenticator.Authenticator
import com.thebrownfoxx.neon.server.service.group.GroupManager
import com.thebrownfoxx.neon.server.service.jwt.JwtProcessor
import com.thebrownfoxx.neon.server.service.member.MemberManager

interface Dependencies {
    val jwtProcessor: JwtProcessor
    val authenticator: Authenticator
    val groupManager: GroupManager
    val memberManager: MemberManager
}
