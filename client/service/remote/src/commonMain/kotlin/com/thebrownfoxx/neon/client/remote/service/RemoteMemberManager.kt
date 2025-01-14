package com.thebrownfoxx.neon.client.remote.service

import com.thebrownfoxx.neon.client.converter.toLocalMember
import com.thebrownfoxx.neon.client.model.LocalMember
import com.thebrownfoxx.neon.client.service.MemberManager
import com.thebrownfoxx.neon.client.service.MemberManager.GetMemberError
import com.thebrownfoxx.neon.client.websocket.WebSocketSubscriber
import com.thebrownfoxx.neon.client.websocket.subscribeAsFlow
import com.thebrownfoxx.neon.common.data.Cache
import com.thebrownfoxx.neon.common.extension.flow.mirrorTo
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.server.route.websocket.member.GetMemberNotFound
import com.thebrownfoxx.neon.server.route.websocket.member.GetMemberRequest
import com.thebrownfoxx.neon.server.route.websocket.member.GetMemberSuccessful
import com.thebrownfoxx.neon.server.route.websocket.member.GetMemberUnexpectedError
import com.thebrownfoxx.outcome.Failure
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.Success
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

class RemoteMemberManager(
    private val subscriber: WebSocketSubscriber,
    externalScope: CoroutineScope,
) : MemberManager {
    private val memberCache = Cache<MemberId, Outcome<LocalMember, GetMemberError>>(externalScope)

    override fun getMember(id: MemberId): Flow<Outcome<LocalMember, GetMemberError>> {
        return memberCache.getAsFlow(id) {
            subscriber.subscribeAsFlow(GetMemberRequest(id = id)) {
                map<GetMemberNotFound> { Failure(GetMemberError.NotFound) }
                map<GetMemberUnexpectedError> { Failure(GetMemberError.UnexpectedError) }
                map<GetMemberSuccessful> { Success(it.member.toLocalMember()) }
            }.mirrorTo(this)
        }
    }
}