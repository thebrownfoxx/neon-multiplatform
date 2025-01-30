package com.thebrownfoxx.neon.client.remote.websocket

import com.thebrownfoxx.neon.client.remote.RemoteGroupManager
import com.thebrownfoxx.neon.client.remote.RemoteGroupManager.GetGroupError
import com.thebrownfoxx.neon.client.remote.RemoteGroupManager.GetMembersError
import com.thebrownfoxx.neon.client.websocket.WebSocketSubscriber
import com.thebrownfoxx.neon.client.websocket.subscribeAsFlow
import com.thebrownfoxx.neon.common.data.Cache
import com.thebrownfoxx.neon.common.extension.flow.mirrorTo
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.server.model.Group
import com.thebrownfoxx.neon.server.route.websocket.group.GetGroupMembersGroupNotFound
import com.thebrownfoxx.neon.server.route.websocket.group.GetGroupMembersRequest
import com.thebrownfoxx.neon.server.route.websocket.group.GetGroupMembersSuccessful
import com.thebrownfoxx.neon.server.route.websocket.group.GetGroupMembersUnexpectedError
import com.thebrownfoxx.neon.server.route.websocket.group.GetGroupNotFound
import com.thebrownfoxx.neon.server.route.websocket.group.GetGroupRequest
import com.thebrownfoxx.neon.server.route.websocket.group.GetGroupSuccessfulChatGroup
import com.thebrownfoxx.neon.server.route.websocket.group.GetGroupSuccessfulCommunity
import com.thebrownfoxx.neon.server.route.websocket.group.GetGroupUnexpectedError
import com.thebrownfoxx.outcome.Failure
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.Success
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

class WebSocketRemoteGroupManager(
    private val subscriber: WebSocketSubscriber,
    externalScope: CoroutineScope,
) : RemoteGroupManager {
    private val groupCache = Cache<GroupId, Outcome<Group, GetGroupError>>(externalScope)
    private val membersCache =
        Cache<GroupId, Outcome<Set<MemberId>, GetMembersError>>(externalScope)

    override fun getGroup(id: GroupId): Flow<Outcome<Group, GetGroupError>> {
        return groupCache.getOrInitialize(id) {
            subscriber.subscribeAsFlow(GetGroupRequest(id = id)) {
                map<GetGroupNotFound> { Failure(GetGroupError.NotFound) }
                map<GetGroupUnexpectedError> { Failure(GetGroupError.UnexpectedError) }
                map<GetGroupSuccessfulChatGroup> { Success(it.chatGroup) }
                map<GetGroupSuccessfulCommunity> { Success(it.community) }
            }.mirrorTo(this)
        }
    }

    override fun getMembers(groupId: GroupId): Flow<Outcome<Set<MemberId>, GetMembersError>> {
        return membersCache.getOrInitialize(groupId) {
            subscriber.subscribeAsFlow(GetGroupMembersRequest(groupId = groupId)) {
                map<GetGroupMembersGroupNotFound> { Failure(GetMembersError.GroupNotFound) }
                map<GetGroupMembersUnexpectedError> { Failure(GetMembersError.UnexpectedError) }
                map<GetGroupMembersSuccessful> { Success(it.members) }
            }.mirrorTo(this)
        }
    }
}