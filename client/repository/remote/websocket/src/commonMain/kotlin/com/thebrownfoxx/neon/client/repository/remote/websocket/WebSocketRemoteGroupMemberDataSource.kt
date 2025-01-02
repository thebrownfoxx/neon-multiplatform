package com.thebrownfoxx.neon.client.repository.remote.websocket

import com.thebrownfoxx.neon.client.repository.remote.RemoteGroupMemberDataSource
import com.thebrownfoxx.neon.client.repository.remote.RemoteGroupMemberDataSource.GetMembersError
import com.thebrownfoxx.neon.common.data.Cache
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.common.websocket.OldWebSocketSession
import com.thebrownfoxx.neon.server.route.websocket.group.GetGroupMembersGroupNotFound
import com.thebrownfoxx.neon.server.route.websocket.group.GetGroupMembersRequest
import com.thebrownfoxx.neon.server.route.websocket.group.GetGroupMembersSuccessful
import com.thebrownfoxx.neon.server.route.websocket.group.GetGroupMembersUnexpectedError
import com.thebrownfoxx.outcome.Failure
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.Success
import com.thebrownfoxx.outcome.map.onFailure
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.plus

class WebSocketRemoteGroupMemberDataSource(
    private val session: OldWebSocketSession,
) : RemoteGroupMemberDataSource {
    private val dataSourceScope = CoroutineScope(Dispatchers.IO) + SupervisorJob()
    private val cache = Cache<GroupId, Outcome<Set<MemberId>, GetMembersError>>(dataSourceScope)

    init {
        session.subscribe<GetGroupMembersGroupNotFound> { response ->
            cache.emit(response.groupId, Failure(GetMembersError.GroupNotFound))
        }
        session.subscribe<GetGroupMembersUnexpectedError> { response ->
            cache.emit(response.groupId, Failure(GetMembersError.UnexpectedError))
        }
        session.subscribe<GetGroupMembersSuccessful> { response ->
            cache.emit(response.groupId, Success(response.members))
        }
    }

    override fun getMembersAsFlow(groupId: GroupId) = cache.getAsFlow(groupId) {
        session.send(GetGroupMembersRequest(groupId = groupId))
            .onFailure { cache.emit(groupId, Failure(GetMembersError.UnexpectedError)) }
    }
}