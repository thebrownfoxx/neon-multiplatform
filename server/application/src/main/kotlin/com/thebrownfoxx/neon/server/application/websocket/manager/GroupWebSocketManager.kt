package com.thebrownfoxx.neon.server.application.websocket.manager

import com.thebrownfoxx.neon.common.outcome.onFailure
import com.thebrownfoxx.neon.common.outcome.onSuccess
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.Uuid
import com.thebrownfoxx.neon.common.websocket.WebSocketManager
import com.thebrownfoxx.neon.common.websocket.WebSocketSession
import com.thebrownfoxx.neon.server.model.ChatGroup
import com.thebrownfoxx.neon.server.model.Community
import com.thebrownfoxx.neon.server.route.websocket.group.GetGroupRequest
import com.thebrownfoxx.neon.server.route.websocket.group.GetGroupResponse
import com.thebrownfoxx.neon.server.service.group.GroupManager
import com.thebrownfoxx.neon.server.service.group.model.GetGroupError
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

class GroupWebSocketManager(
    session: WebSocketSession,
    private val groupManager: GroupManager,
) : WebSocketManager(session) {
    private val getGroupJobs = ConcurrentHashMap<GroupId, Job>()

    init {
        subscribe<GetGroupRequest>(GetGroupRequest.Label) { request ->
            getGroup(request.id)
        }
    }

    private fun getGroup(id: GroupId) {
        getGroupJobs[id]?.cancel()
        getGroupJobs[id] = observerScope.launch {
            val jobId = Uuid()
            groupManager.getGroup(id).collect { groupOutcome ->
                println("Collected in $jobId")
                groupOutcome.onSuccess { group ->
                    when (group) {
                        is ChatGroup -> send(GetGroupResponse.SuccessfulChatGroup(group))
                        is Community -> send(GetGroupResponse.SuccessfulCommunity(group))
                    }
                }.onFailure { error ->
                    when (error) {
                        is GetGroupError.NotFound -> send(GetGroupResponse.NotFound(id))
                        GetGroupError.ConnectionError -> send(GetGroupResponse.ConnectionError(id))
                    }
                }
            }
        }
    }
}