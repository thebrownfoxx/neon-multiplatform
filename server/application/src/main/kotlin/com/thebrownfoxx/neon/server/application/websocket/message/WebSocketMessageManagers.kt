package com.thebrownfoxx.neon.server.application.websocket.message

import com.thebrownfoxx.neon.server.application.websocket.KtorServerWebSocketSession
import com.thebrownfoxx.neon.server.service.GroupManager
import com.thebrownfoxx.neon.server.service.MemberManager
import com.thebrownfoxx.neon.server.service.Messenger

class WebSocketMessageManagers private constructor(
    private val session: KtorServerWebSocketSession,
    private val groupManager: GroupManager,
    private val memberManager: MemberManager,
    private val messenger: Messenger,
) {
    companion object {
        suspend fun startListening(
            session: KtorServerWebSocketSession,
            groupManager: GroupManager,
            memberManager: MemberManager,
            messenger: Messenger,
        ) = WebSocketMessageManagers(
            session,
            groupManager,
            memberManager,
            messenger,
        ).apply { startListening() }
    }

    private suspend fun startListening() {
        GroupWebSocketMessageManager.startListening(session, groupManager)
        MemberWebSocketMessageManager.startListening(session, memberManager)
        MessageWebSocketMessageManager.startListening(session, messenger)
    }
}