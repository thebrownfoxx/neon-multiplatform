package com.thebrownfoxx.neon.server.application.websocket.message

import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.common.websocket.WebSocketSession
import com.thebrownfoxx.neon.server.route.websocket.member.GetMemberNotFound
import com.thebrownfoxx.neon.server.route.websocket.member.GetMemberRequest
import com.thebrownfoxx.neon.server.route.websocket.member.GetMemberSuccessful
import com.thebrownfoxx.neon.server.route.websocket.member.GetMemberUnexpectedError
import com.thebrownfoxx.neon.server.service.MemberManager
import com.thebrownfoxx.neon.server.service.MemberManager.GetMemberError
import com.thebrownfoxx.outcome.onFailure
import com.thebrownfoxx.outcome.onSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.plus

class MemberWebSocketMessageManager(
    private val session: WebSocketSession,
    private val memberManager: MemberManager,
) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO) + SupervisorJob()

    private val getMemberJobManager = JobManager<MemberId>(coroutineScope, session.close)

    init {
        session.subscribe<GetMemberRequest> { request ->
            getMember(request.id)
        }
    }

    private fun getMember(id: MemberId) {
        getMemberJobManager[id] = {
            memberManager.getMember(id).collect { memberOutcome ->
                memberOutcome.onSuccess { member ->
                    session.send(GetMemberSuccessful(member))
                }.onFailure {
                    when (error) {
                        GetMemberError.NotFound -> session.send(GetMemberNotFound(id))
                        GetMemberError.UnexpectedError -> session.send(GetMemberUnexpectedError(id))
                    }
                }
            }
        }
    }
}