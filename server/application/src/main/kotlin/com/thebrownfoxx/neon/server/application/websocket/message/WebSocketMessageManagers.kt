package com.thebrownfoxx.neon.server.application.websocket.message

import com.thebrownfoxx.neon.server.application.websocket.KtorServerWebSocketSession
import com.thebrownfoxx.neon.server.service.GroupManager
import com.thebrownfoxx.neon.server.service.MemberManager
import com.thebrownfoxx.neon.server.service.Messenger
import kotlinx.coroutines.CoroutineScope

class WebSocketMessageManagers(
    session: KtorServerWebSocketSession,
    groupManager: GroupManager,
    memberManager: MemberManager,
    messenger: Messenger,
    externalScope: CoroutineScope,
) {
    init {
        GroupWebSocketMessageManager(session, groupManager, externalScope)
        MemberWebSocketMessageManager(session, memberManager, externalScope)
        MessageWebSocketMessageManager(session, messenger, externalScope)
    }
}