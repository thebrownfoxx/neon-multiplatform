package com.thebrownfoxx.neon.server.application.websocket.message

import com.thebrownfoxx.neon.common.websocket.WebSocketSession
import com.thebrownfoxx.neon.server.service.group.GroupManager
import com.thebrownfoxx.neon.server.service.member.MemberManager

class WebSocketMessageManagers(
    session: WebSocketSession,
    groupManager: GroupManager,
    memberManager: MemberManager,
) {
    init {
        GroupWebSocketMessageManager(session, groupManager)
        MemberWebSocketMessageManager(session, memberManager)
    }
}