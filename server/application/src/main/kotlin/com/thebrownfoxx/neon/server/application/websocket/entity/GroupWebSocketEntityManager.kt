package com.thebrownfoxx.neon.server.application.websocket.entity

import com.thebrownfoxx.neon.common.outcome.onFailure
import com.thebrownfoxx.neon.common.outcome.onSuccess
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.websocket.WebSocketSession
import com.thebrownfoxx.neon.server.model.ChatGroup
import com.thebrownfoxx.neon.server.model.Community
import com.thebrownfoxx.neon.server.route.websocket.group.GetGroupRequest
import com.thebrownfoxx.neon.server.route.websocket.group.GetGroupResponse
import com.thebrownfoxx.neon.server.service.group.GroupManager
import com.thebrownfoxx.neon.server.service.group.model.GetGroupError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import java.util.concurrent.ConcurrentHashMap

class GroupWebSocketEntityManager(
    private val session: WebSocketSession,
    private val groupManager: GroupManager,
) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO) + SupervisorJob()

    private val getGroupJobs = ConcurrentHashMap<GroupId, Job>()

    init {
        session.subscribe<GetGroupRequest> { request ->
            getGroup(request.id)
        }

        coroutineScope.launch {
            session.close.collect {
                getGroupJobs.values.forEach { it.cancel() }
            }
        }
    }

    private fun getGroup(id: GroupId) {
        getGroupJobs[id]?.cancel()
        with(session) {
            getGroupJobs[id] = coroutineScope.launch {
                groupManager.getGroup(id).collect { groupOutcome ->
                    groupOutcome.onSuccess { group ->
                        when (group) {
                            is ChatGroup -> send(GetGroupResponse.SuccessfulChatGroup(group))
                            is Community -> send(GetGroupResponse.SuccessfulCommunity(group))
                        }
                    }.onFailure { error ->
                        when (error) {
                            is GetGroupError.NotFound -> send(GetGroupResponse.NotFound(id))
                            GetGroupError.ConnectionError ->
                                send(GetGroupResponse.ConnectionError(id))
                        }
                    }
                }
            }
        }
    }
}