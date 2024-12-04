package com.thebrownfoxx.neon.client.repository.remote.websocket

import com.thebrownfoxx.neon.client.repository.remote.GetGroupError
import com.thebrownfoxx.neon.client.repository.remote.RemoteGroupDataSource
import com.thebrownfoxx.neon.common.data.Cache
import com.thebrownfoxx.neon.common.outcome.Failure
import com.thebrownfoxx.neon.common.outcome.Outcome
import com.thebrownfoxx.neon.common.outcome.Success
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.websocket.WebSocketSession
import com.thebrownfoxx.neon.server.model.Group
import com.thebrownfoxx.neon.server.route.websocket.group.GetGroupInternalError
import com.thebrownfoxx.neon.server.route.websocket.group.GetGroupNotFound
import com.thebrownfoxx.neon.server.route.websocket.group.GetGroupRequest
import com.thebrownfoxx.neon.server.route.websocket.group.GetGroupSuccessfulChatGroup
import com.thebrownfoxx.neon.server.route.websocket.group.GetGroupSuccessfulCommunity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.plus

class WebSocketRemoteGroupDataSource(
    private val session: WebSocketSession,
) : RemoteGroupDataSource {
    private val dataSourceScope = CoroutineScope(Dispatchers.IO) + SupervisorJob()
    private val cache = Cache<GroupId, Outcome<Group, GetGroupError>>(dataSourceScope)

    init {
        session.subscribe<GetGroupNotFound> { response ->
            cache.emit(response.id, Failure(GetGroupError.NotFound))
        }
        session.subscribe<GetGroupInternalError> { response ->
            cache.emit(response.id, Failure(GetGroupError.ServerError))
        }
        session.subscribe<GetGroupSuccessfulChatGroup> { response ->
            cache.emit(response.chatGroup.id, Success(response.chatGroup))
        }
        session.subscribe<GetGroupSuccessfulCommunity> { response ->
            cache.emit(response.community.id, Success(response.community))
        }
    }

    override fun getAsFlow(id: GroupId): Flow<Outcome<Group, GetGroupError>> = cache.getAsFlow(id) {
        session.send(GetGroupRequest(id))
    }
}