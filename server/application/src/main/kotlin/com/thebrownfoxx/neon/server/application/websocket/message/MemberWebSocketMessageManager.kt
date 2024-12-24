package com.thebrownfoxx.neon.server.application.websocket.message

import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.common.websocket.WebSocketSession
import com.thebrownfoxx.neon.common.websocket.listen
import com.thebrownfoxx.neon.common.websocket.send
import com.thebrownfoxx.neon.server.route.websocket.member.GetMemberNotFound
import com.thebrownfoxx.neon.server.route.websocket.member.GetMemberRequest
import com.thebrownfoxx.neon.server.route.websocket.member.GetMemberSuccessful
import com.thebrownfoxx.neon.server.route.websocket.member.GetMemberUnexpectedError
import com.thebrownfoxx.neon.server.service.MemberManager
import com.thebrownfoxx.neon.server.service.MemberManager.GetMemberError
import com.thebrownfoxx.outcome.map.onFailure
import com.thebrownfoxx.outcome.map.onSuccess
import kotlinx.coroutines.supervisorScope

class MemberWebSocketMessageManager private constructor(
    private val session: WebSocketSession,
    private val memberManager: MemberManager,
) {
    companion object {
        suspend fun startListening(
            session: WebSocketSession,
            memberManager: MemberManager,
        ) = MemberWebSocketMessageManager(session, memberManager).apply { startListening() }
    }

    private lateinit var getMemberJobManager: JobManager<MemberId>

    private suspend fun startListening() {
        supervisorScope {
            getMemberJobManager = JobManager(this)

            session.listen<GetMemberRequest>(this) { request ->
                getMember(request.id)
            }
        }
    }

    private fun getMember(id: MemberId) {
        getMemberJobManager[id] = {
            memberManager.getMember(id).collect { memberOutcome ->
                memberOutcome.onSuccess { member ->
                    session.send(GetMemberSuccessful(member))
                }.onFailure { error ->
                    when (error) {
                        GetMemberError.NotFound -> session.send(GetMemberNotFound(id))
                        GetMemberError.UnexpectedError -> session.send(GetMemberUnexpectedError(id))
                    }
                }
            }
        }
    }
}