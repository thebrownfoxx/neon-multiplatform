package com.thebrownfoxx.neon.client.repository.remote.websocket

import com.thebrownfoxx.neon.client.repository.remote.RemoteGroupDataSource
import com.thebrownfoxx.neon.common.data.Cache
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.type.Failure
import com.thebrownfoxx.neon.common.type.Outcome
import com.thebrownfoxx.neon.common.type.Success
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.websocket.WebSocketManager
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
    session: WebSocketSession,
) : RemoteGroupDataSource, WebSocketManager(session) {
    private val dataSourceScope = CoroutineScope(Dispatchers.IO) + SupervisorJob()
    private val cache = Cache<GroupId, Outcome<Group, GetError>>(dataSourceScope)

    init {
        with(cache) {
            subscribe<Response.NotFound>(Response.NotFound.Label) { response ->
                emit(response.id, Failure(GetError.NotFound))
            }
            subscribe<Response.ConnectionError>(Response.ConnectionError.Label) { response ->
                emit(response.id, Failure(GetError.ConnectionError))
            }
            subscribe<Response.SuccessfulChatGroup>(Response.SuccessfulChatGroup.Label) { response ->
                emit(response.chatGroup.id, Success(response.chatGroup))
            }
            subscribe<Response.SuccessfulCommunity>(Response.SuccessfulCommunity.Label) { response ->
                emit(response.community.id, Success(response.community))
            }
        }
    }

    override fun get(id: GroupId): Flow<Outcome<Group, GetError>> = cache.getAsFlow(id) {
        dataSourceScope.launch { send(GetGroupRequest(id)) }
    }
}