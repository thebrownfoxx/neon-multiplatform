package com.thebrownfoxx.neon.server.application.websocket.manager

import com.thebrownfoxx.neon.common.websocket.WebSocketSession
import com.thebrownfoxx.neon.server.service.group.GroupManager

class WebSocketManagers(
    session: WebSocketSession,
    groupManager: GroupManager,
) {
    init {
        GroupWebSocketManager(session, groupManager)
    }
}