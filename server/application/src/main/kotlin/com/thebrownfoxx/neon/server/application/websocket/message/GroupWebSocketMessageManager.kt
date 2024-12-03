package com.thebrownfoxx.neon.server.application.websocket.message

import com.thebrownfoxx.neon.common.outcome.onFailure
import com.thebrownfoxx.neon.common.outcome.onSuccess
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.websocket.WebSocketSession
import com.thebrownfoxx.neon.server.model.ChatGroup
import com.thebrownfoxx.neon.server.model.Community
import com.thebrownfoxx.neon.server.route.websocket.group.GetGroupConnectionError
import com.thebrownfoxx.neon.server.route.websocket.group.GetGroupNotFound
import com.thebrownfoxx.neon.server.route.websocket.group.GetGroupRequest
import com.thebrownfoxx.neon.server.route.websocket.group.GetGroupSuccessfulChatGroup
import com.thebrownfoxx.neon.server.route.websocket.group.GetGroupSuccessfulCommunity
import com.thebrownfoxx.neon.server.service.group.GroupManager
import com.thebrownfoxx.neon.server.service.group.model.GetGroupError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.plus

class GroupWebSocketMessageManager(
    private val session: WebSocketSession,
    private val groupManager: GroupManager,
) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO) + SupervisorJob()

    private val getGroupJobManager = JobManager<GroupId>(coroutineScope, session.close)

    init {
        session.subscribe<GetGroupRequest> { request ->
            getGroup(request.id)
        }
    }

    private fun getGroup(id: GroupId) {
        getGroupJobManager[id] = {
            with(session) {
                groupManager.getGroup(id).collect { groupOutcome ->
                    groupOutcome.onSuccess { group ->
                        when (group) {
                            is ChatGroup -> send(GetGroupSuccessfulChatGroup(group))
                            is Community -> send(GetGroupSuccessfulCommunity(group))
                        }
                    }.onFailure { error ->
                        when (error) {
                            GetGroupError.NotFound -> send(GetGroupNotFound(id))
                            GetGroupError.ConnectionError ->
                                send(GetGroupConnectionError(id))
                        }
                    }
                }
            }
        }
    }
}