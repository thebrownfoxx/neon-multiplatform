package com.thebrownfoxx.neon.server.application.websocket.message

import com.thebrownfoxx.neon.server.application.websocket.KtorServerWebSocketSession
import com.thebrownfoxx.neon.server.service.GroupManager
import com.thebrownfoxx.neon.server.service.MemberManager
import com.thebrownfoxx.neon.server.service.Messenger

class WebSocketMessageManagers(
    session: KtorServerWebSocketSession,
    groupManager: GroupManager,
    memberManager: MemberManager,
    messenger: Messenger,
) {
    init {
        GroupWebSocketMessageManager(session, groupManager)
        MemberWebSocketMessageManager(session, memberManager)
        MessageWebSocketMessageManager(session, messenger)
    }
}