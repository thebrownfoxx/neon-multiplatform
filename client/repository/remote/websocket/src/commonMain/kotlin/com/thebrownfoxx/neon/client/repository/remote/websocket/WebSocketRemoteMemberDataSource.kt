package com.thebrownfoxx.neon.client.repository.remote.websocket

import com.thebrownfoxx.neon.client.repository.remote.GetMemberError
import com.thebrownfoxx.neon.client.repository.remote.RemoteMemberDataSource
import com.thebrownfoxx.neon.common.data.Cache
import com.thebrownfoxx.neon.common.outcome.Failure
import com.thebrownfoxx.neon.common.outcome.Outcome
import com.thebrownfoxx.neon.common.outcome.Success
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.common.websocket.WebSocketSession
import com.thebrownfoxx.neon.server.model.Member
import com.thebrownfoxx.neon.server.route.websocket.member.GetMemberInternalError
import com.thebrownfoxx.neon.server.route.websocket.member.GetMemberNotFound
import com.thebrownfoxx.neon.server.route.websocket.member.GetMemberRequest
import com.thebrownfoxx.neon.server.route.websocket.member.GetMemberSuccessful
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.plus

class WebSocketRemoteMemberDataSource(
    private val session: WebSocketSession,
) : RemoteMemberDataSource {
    private val dataSourceScope = CoroutineScope(Dispatchers.IO) + SupervisorJob()
    private val cache = Cache<MemberId, Outcome<Member, GetMemberError>>(dataSourceScope)

    init {
        session.subscribe<GetMemberNotFound> { response ->
            cache.emit(response.id, Failure(GetMemberError.NotFound))
        }
        session.subscribe<GetMemberInternalError> { response ->
            cache.emit(response.id, Failure(GetMemberError.ServerError))
        }
        session.subscribe<GetMemberSuccessful> { response ->
            cache.emit(response.member.id, Success(response.member))
        }
    }

    override fun getAsFlow(id: MemberId): Flow<Outcome<Member, GetMemberError>> =
        cache.getAsFlow(id) {
            session.send(GetMemberRequest(id))
        }
}