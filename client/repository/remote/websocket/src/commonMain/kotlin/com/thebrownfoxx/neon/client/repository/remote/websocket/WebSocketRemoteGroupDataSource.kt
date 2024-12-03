package com.thebrownfoxx.neon.client.repository.remote.websocket

import com.thebrownfoxx.neon.client.repository.remote.RemoteGroupDataSource
import com.thebrownfoxx.neon.common.data.Cache
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.outcome.Failure
import com.thebrownfoxx.neon.common.outcome.Outcome
import com.thebrownfoxx.neon.common.outcome.Success
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.websocket.WebSocketSession
import com.thebrownfoxx.neon.server.model.Group
import com.thebrownfoxx.neon.server.route.websocket.group.GetGroupRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import com.thebrownfoxx.neon.server.route.websocket.group.GetGroupResponse as Response

class WebSocketRemoteGroupDataSource(
    private val session: WebSocketSession,
) : RemoteGroupDataSource {
    private val dataSourceScope = CoroutineScope(Dispatchers.IO) + SupervisorJob()
    private val cache = Cache<GroupId, Outcome<Group, GetError>>(dataSourceScope)

    init {
        with(cache) {
            session.subscribe<Response.NotFound> { response ->
                emit(response.id, Failure(GetError.NotFound))
            }
            session.subscribe<Response.ConnectionError> { response ->
                emit(response.id, Failure(GetError.ConnectionError))
            }
            session.subscribe<Response.SuccessfulChatGroup> { response ->
                emit(response.chatGroup.id, Success(response.chatGroup))
            }
            session.subscribe<Response.SuccessfulCommunity> { response ->
                emit(response.community.id, Success(response.community))
            }
        }
    }

    override fun getAsFlow(id: GroupId): Flow<Outcome<Group, GetError>> = cache.getAsFlow(id) {
        dataSourceScope.launch { session.send(GetGroupRequest(id)) }
    }
}