package com.thebrownfoxx.neon.server.application.websocket.message

import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.websocket.WebSocketSession
import com.thebrownfoxx.neon.common.websocket.listen
import com.thebrownfoxx.neon.common.websocket.send
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
import kotlinx.coroutines.supervisorScope

class GroupWebSocketMessageManager private constructor(
    private val session: WebSocketSession,
    private val groupManager: GroupManager,
) {
    companion object {
        suspend fun startListening(
            session: WebSocketSession,
            groupManager: GroupManager,
        ) = GroupWebSocketMessageManager(session, groupManager).apply { startListening() }
    }

    private lateinit var getGroupJobManager: JobManager<GroupId>
    private lateinit var getMembersJobManager: JobManager<GroupId>

    private suspend fun startListening() {
        supervisorScope {
            getGroupJobManager = JobManager(this)
            getMembersJobManager = JobManager(this)

            session.listen<GetGroupRequest>(this) { request ->
                getGroup(request.id)
            }
            session.listen<GetGroupMembersRequest>(this) { request ->
                getMembers(request.groupId)
            }
        }
    }

    private fun getGroup(id: GroupId) {
        getGroupJobManager[id] = {
            groupManager.getGroup(id).collect { groupOutcome ->
                groupOutcome.onSuccess { group ->
                    when (group) {
                        is ChatGroup -> session.send(GetGroupSuccessfulChatGroup(group))
                        is Community -> session.send(GetGroupSuccessfulCommunity(group))
                    }
                }.onFailure { error ->
                    when (error) {
                        GetGroupError.NotFound -> session.send(GetGroupNotFound(id))
                        GetGroupError.UnexpectedError ->
                            session.send(GetGroupUnexpectedError(id))
                    }
                }
            }
        }
    }

    private fun getMembers(groupId: GroupId) {
        getMembersJobManager[groupId] = {
            groupManager.getMembers(groupId).collect { membersOutcome ->
                membersOutcome.onSuccess { members ->
                    session.send(GetGroupMembersSuccessful(groupId, members))
                }.onFailure { error ->
                    when (error) {
                        GroupManager.GetMembersError.GroupNotFound ->
                            session.send(GetGroupNotFound(groupId))

                        GroupManager.GetMembersError.UnexpectedError ->
                            session.send(GetGroupUnexpectedError(groupId))
                    }
                }
            }
        }
    }
}