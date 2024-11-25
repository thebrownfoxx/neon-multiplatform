package com.thebrownfoxx.neon.client.repository.remote.websocket

import com.thebrownfoxx.neon.client.repository.remote.group.RemoteGroupDataSource
import com.thebrownfoxx.neon.client.repository.remote.group.model.RemoteGetGroupError
import com.thebrownfoxx.neon.common.type.Failure
import com.thebrownfoxx.neon.common.type.Outcome
import com.thebrownfoxx.neon.common.type.Success
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.websocket.WebSocketObserver
import com.thebrownfoxx.neon.common.websocket.WebSocketSession
import com.thebrownfoxx.neon.server.model.Group
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import com.thebrownfoxx.neon.server.route.websocket.group.GetGroupResponse as Response

class WebSocketRemoteGroupDataSource(
    session: WebSocketSession,
) : RemoteGroupDataSource, WebSocketObserver(session) {
    private val flows: MutableMap<GroupId, MutableSharedFlow<Outcome<Group, RemoteGetGroupError>>> =
        HashMap()

    init {
        subscribe<Response.NotFound>(Response.NotFound.Label) { response ->
            emit(response.id, Failure(RemoteGetGroupError.NotFound))
        }
        subscribe<Response.ConnectionError>(Response.ConnectionError.Label) { response ->
            emit(response.id, Failure(RemoteGetGroupError.ConnectionError))
        }
        subscribe<Response.SuccessfulChatGroup>(Response.SuccessfulChatGroup.Label) { response ->
            emit(response.chatGroup.id, Success(response.chatGroup))
        }
        subscribe<Response.SuccessfulCommunity>(Response.SuccessfulCommunity.Label) { response ->
            emit(response.community.id, Success(response.community))
        }
    }

    private fun emit(id: GroupId, outcome: Outcome<Group, RemoteGetGroupError>) {
        flows[id]?.let { coroutineScope.launch { it.emit(outcome) } }
    }

    override fun get(id: GroupId): Flow<Outcome<Group, RemoteGetGroupError>> {
        val savedFlow = flows[id]
        if (savedFlow != null) return savedFlow

        val sharedFlow = MutableSharedFlow<Outcome<Group, RemoteGetGroupError>>(replay = 1)
        flows[id] = sharedFlow
        return sharedFlow
    }
}