package com.thebrownfoxx.neon.client.remote.websocket

import com.thebrownfoxx.neon.client.remote.RemoteMemberManager
import com.thebrownfoxx.neon.client.remote.RemoteMemberManager.GetMemberError
import com.thebrownfoxx.neon.client.websocket.WebSocketSubscriber
import com.thebrownfoxx.neon.client.websocket.subscribeAsFlow
import com.thebrownfoxx.neon.common.data.cacheIn
import com.thebrownfoxx.neon.common.data.flowCacheMap
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.server.model.Member
import com.thebrownfoxx.neon.server.route.websocket.member.GetMemberNotFound
import com.thebrownfoxx.neon.server.route.websocket.member.GetMemberRequest
import com.thebrownfoxx.neon.server.route.websocket.member.GetMemberSuccessful
import com.thebrownfoxx.neon.server.route.websocket.member.GetMemberUnexpectedError
import com.thebrownfoxx.outcome.Failure
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.Success
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asSharedFlow

class WebSocketRemoteMemberManager(
    private val subscriber: WebSocketSubscriber,
    private val externalScope: CoroutineScope,
) : RemoteMemberManager {
    private val memberCache = flowCacheMap<MemberId, Outcome<Member, GetMemberError>>(externalScope)

    override fun getMember(id: MemberId): Flow<Outcome<Member, GetMemberError>> {
        return memberCache.getOrPut(id) {
            subscriber.subscribeAsFlow(GetMemberRequest(id = id)) {
                map<GetMemberNotFound> { Failure(GetMemberError.NotFound) }
                map<GetMemberUnexpectedError> { Failure(GetMemberError.UnexpectedError) }
                map<GetMemberSuccessful> { Success(it.member) }
            }.cacheIn(externalScope)
        }.asSharedFlow()
    }
}