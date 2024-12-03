package com.thebrownfoxx.neon.server.application.websocket.message

import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.common.type.id.MessageId
import com.thebrownfoxx.neon.common.websocket.WebSocketSession
import com.thebrownfoxx.neon.server.service.messenger.Messenger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

class MessageWebSocketMessageManager(
    private val session: WebSocketSession,
    private val messenger: Messenger,
) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO) + SupervisorJob()

    private val getMessageJobManager = JobManager<MessageId>(coroutineScope, session.close)
    private val getConversationPreviewJobManager =
        JobManager<GroupId>(coroutineScope, session.close)

    private fun getConversations(actorId: MemberId) {
        coroutineScope.launch {
//            messenger.oldGetConversations(actorId).onSuccess { conversations ->
//
//            }
        }
    }
}