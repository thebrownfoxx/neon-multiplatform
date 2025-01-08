package com.thebrownfoxx.neon.client.remote.service

import com.thebrownfoxx.neon.client.converter.toLocalChatGroup
import com.thebrownfoxx.neon.client.converter.toLocalCommunity
import com.thebrownfoxx.neon.client.model.LocalGroup
import com.thebrownfoxx.neon.client.service.GroupManager
import com.thebrownfoxx.neon.client.service.GroupManager.GetGroupError
import com.thebrownfoxx.neon.client.service.GroupManager.GetMembersError
import com.thebrownfoxx.neon.client.websocket.WebSocketSubscriber
import com.thebrownfoxx.neon.client.websocket.subscribeAsFlow
import com.thebrownfoxx.neon.common.data.Cache
import com.thebrownfoxx.neon.common.extension.mirrorTo
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
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

class RemoteGroupManager(
    private val subscriber: WebSocketSubscriber,
    externalScope: CoroutineScope,
) : GroupManager {
    private val groupCache = Cache<GroupId, Outcome<LocalGroup, GetGroupError>>(externalScope)
    private val membersCache =
        Cache<GroupId, Outcome<Set<MemberId>, GetMembersError>>(externalScope)

    override fun getGroup(id: GroupId): Flow<Outcome<LocalGroup, GetGroupError>> {
        return groupCache.getAsFlow(id) {
            subscriber.subscribeAsFlow(GetGroupRequest(id = id)) {
                map<GetGroupNotFound> { Failure(GetGroupError.NotFound) }
                map<GetGroupUnexpectedError> { Failure(GetGroupError.UnexpectedError) }
                map<GetGroupSuccessfulChatGroup> { Success(it.chatGroup.toLocalChatGroup()) }
                map<GetGroupSuccessfulCommunity> { Success(it.community.toLocalCommunity()) }
            }.mirrorTo(this)
        }
    }

    override fun getMembers(groupId: GroupId): Flow<Outcome<Set<MemberId>, GetMembersError>> {
        return membersCache.getAsFlow(groupId) {
            subscriber.subscribeAsFlow(GetGroupMembersRequest(groupId = groupId)) {
                map<GetGroupMembersGroupNotFound> { Failure(GetMembersError.GroupNotFound) }
                map<GetGroupMembersUnexpectedError> { Failure(GetMembersError.UnexpectedError) }
                map<GetGroupMembersSuccessful> { Success(it.members) }
            }.mirrorTo(this)
        }
    }
}