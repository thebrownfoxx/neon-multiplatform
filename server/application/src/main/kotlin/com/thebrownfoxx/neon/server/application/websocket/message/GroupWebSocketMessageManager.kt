package com.thebrownfoxx.neon.server.application.websocket.message

import com.thebrownfoxx.neon.common.data.websocket.WebSocketSession
import com.thebrownfoxx.neon.common.data.websocket.listen
import com.thebrownfoxx.neon.common.data.websocket.send
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.server.model.ChatGroup
import com.thebrownfoxx.neon.server.model.Community
import com.thebrownfoxx.neon.server.route.websocket.group.GetGroupMembersRequest
import com.thebrownfoxx.neon.server.route.websocket.group.GetGroupMembersSuccessful
import com.thebrownfoxx.neon.server.route.websocket.group.GetGroupNotFound
import com.thebrownfoxx.neon.server.route.websocket.group.GetGroupRequest
import com.thebrownfoxx.neon.server.route.websocket.group.GetGroupSuccessfulChatGroup
import com.thebrownfoxx.neon.server.route.websocket.group.GetGroupSuccessfulCommunity
import com.thebrownfoxx.neon.server.route.websocket.group.GetGroupUnexpectedError
import com.thebrownfoxx.neon.server.service.GroupManager
import com.thebrownfoxx.neon.server.service.GroupManager.GetGroupError
import com.thebrownfoxx.outcome.map.onFailure
import com.thebrownfoxx.outcome.map.onSuccess
import kotlinx.coroutines.CoroutineScope

class GroupWebSocketMessageManager(
    private val session: WebSocketSession,
    private val groupManager: GroupManager,
    externalScope: CoroutineScope,
) {
    private val getGroupJobManager = JobManager<GroupId>(externalScope)
    private val getMembersJobManager = JobManager<GroupId>(externalScope)

    init {
        session.listen<GetGroupRequest>(externalScope) { it.fulfill() }
        session.listen<GetGroupMembersRequest>(externalScope) { it.fulfill() }
    }

    private fun GetGroupRequest.fulfill() {
        getGroupJobManager[id] = {
            groupManager.getGroup(id).collect { groupOutcome ->
                groupOutcome.onSuccess { group ->
                    when (group) {
                        is ChatGroup -> session.send(GetGroupSuccessfulChatGroup(requestId, group))
                        is Community -> session.send(GetGroupSuccessfulCommunity(requestId, group))
                    }
                }.onFailure { error ->
                    when (error) {
                        GetGroupError.NotFound -> session.send(GetGroupNotFound(requestId, id))
                        GetGroupError.UnexpectedError ->
                            session.send(GetGroupUnexpectedError(requestId, id))
                    }
                }
            }
        }
    }

    private fun GetGroupMembersRequest.fulfill() {
        getMembersJobManager[groupId] = {
            groupManager.getMembers(groupId).collect { membersOutcome ->
                membersOutcome.onSuccess { members ->
                    session.send(GetGroupMembersSuccessful(requestId, groupId, members))
                }.onFailure { error ->
                    when (error) {
                        GroupManager.GetMembersError.GroupNotFound ->
                            session.send(GetGroupNotFound(requestId, groupId))

                        GroupManager.GetMembersError.UnexpectedError ->
                            session.send(GetGroupUnexpectedError(requestId, groupId))
                    }
                }
            }
        }
    }
}