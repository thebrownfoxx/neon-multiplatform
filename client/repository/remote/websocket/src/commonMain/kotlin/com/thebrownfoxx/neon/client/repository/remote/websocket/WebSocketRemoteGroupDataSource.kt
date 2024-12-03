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
import com.thebrownfoxx.neon.server.route.websocket.group.GetGroupConnectionError
import com.thebrownfoxx.neon.server.route.websocket.group.GetGroupNotFound
import com.thebrownfoxx.neon.server.route.websocket.group.GetGroupRequest
import com.thebrownfoxx.neon.server.route.websocket.group.GetGroupSuccessfulChatGroup
import com.thebrownfoxx.neon.server.route.websocket.group.GetGroupSuccessfulCommunity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

class WebSocketRemoteGroupDataSource(
    private val session: WebSocketSession,
) : RemoteGroupDataSource {
    private val dataSourceScope = CoroutineScope(Dispatchers.IO) + SupervisorJob()
    private val cache = Cache<GroupId, Outcome<Group, GetError>>(dataSourceScope)

    init {
        with(cache) {
            session.subscribe<GetGroupNotFound> { response ->
                emit(response.id, Failure(GetError.NotFound))
            }
            session.subscribe<GetGroupConnectionError> { response ->
                emit(response.id, Failure(GetError.ConnectionError))
            }
            session.subscribe<GetGroupSuccessfulChatGroup> { response ->
                emit(response.chatGroup.id, Success(response.chatGroup))
            }
            session.subscribe<GetGroupSuccessfulCommunity> { response ->
                emit(response.community.id, Success(response.community))
            }
        }
    }

    override fun getAsFlow(id: GroupId): Flow<Outcome<Group, GetError>> = cache.getAsFlow(id) {
        dataSourceScope.launch { session.send(GetGroupRequest(id)) }
    }
}