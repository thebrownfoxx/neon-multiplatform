package com.thebrownfoxx.neon.client.repository.remote.websocket

import com.thebrownfoxx.neon.client.repository.remote.RemoteMemberDataSource
import com.thebrownfoxx.neon.common.data.Cache
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.common.websocket.OldWebSocketSession
import com.thebrownfoxx.neon.server.model.Member
import com.thebrownfoxx.neon.server.route.websocket.member.GetMemberNotFound
import com.thebrownfoxx.neon.server.route.websocket.member.GetMemberRequest
import com.thebrownfoxx.neon.server.route.websocket.member.GetMemberSuccessful
import com.thebrownfoxx.neon.server.route.websocket.member.GetMemberUnexpectedError
import com.thebrownfoxx.outcome.Failure
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.Success
import com.thebrownfoxx.outcome.map.onFailure
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.plus

class WebSocketRemoteMemberDataSource(
    private val session: OldWebSocketSession,
) : RemoteMemberDataSource {
    private val dataSourceScope = CoroutineScope(Dispatchers.IO) + SupervisorJob()
    private val cache = Cache<MemberId, Outcome<Member, GetError>>(dataSourceScope)

    init {
        session.subscribe<GetMemberNotFound> { response ->
            cache.emit(response.id, Failure(GetError.NotFound))
        }
        session.subscribe<GetMemberUnexpectedError> { response ->
            cache.emit(response.id, Failure(GetError.UnexpectedError))
        }
        session.subscribe<GetMemberSuccessful> { response ->
            cache.emit(response.member.id, Success(response.member))
        }
    }

    override fun getAsFlow(id: MemberId) = cache.getAsFlow(id) {
        session.send(GetMemberRequest(id))
            .onFailure { cache.emit(id, Failure(GetError.ConnectionError)) }
    }
}