package com.thebrownfoxx.neon.server.application.websocket.entity

import com.thebrownfoxx.neon.common.websocket.WebSocketSession
import com.thebrownfoxx.neon.server.service.group.GroupManager

class WebSocketEntityManagers(
    session: WebSocketSession,
    groupManager: GroupManager,
) {
    init {
        GroupWebSocketEntityManager(session, groupManager)
    }
}