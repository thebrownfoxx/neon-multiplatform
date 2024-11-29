package com.thebrownfoxx.neon.server.application.websocket.fetcher

import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.onFailure
import com.thebrownfoxx.neon.common.type.onSuccess
import com.thebrownfoxx.neon.common.websocket.WebSocketObserver
import com.thebrownfoxx.neon.common.websocket.WebSocketSession
import com.thebrownfoxx.neon.server.model.ChatGroup
import com.thebrownfoxx.neon.server.model.Community
import com.thebrownfoxx.neon.server.route.websocket.group.GetGroupRequest
import com.thebrownfoxx.neon.server.route.websocket.group.GetGroupResponse
import com.thebrownfoxx.neon.server.service.group.GroupManager
import com.thebrownfoxx.neon.server.service.group.model.GetGroupError

class WebSocketGroupFetcher(
    session: WebSocketSession,
    private val groupManager: GroupManager,
) : WebSocketObserver(session) {
    init {
        subscribe<GetGroupRequest>(GetGroupRequest.Label) { request ->
            getGroup(request.id)
        }
    }

    private suspend fun getGroup(id: GroupId) {
        groupManager.getGroup(id).collect { groupOutcome ->
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