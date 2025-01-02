package com.thebrownfoxx.neon.server.route.websocket.group

import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.common.websocket.model.RequestId
import com.thebrownfoxx.neon.common.websocket.model.WebSocketMessage
import kotlinx.serialization.Serializable

@Serializable
data class GetGroupMembersRequest(
    override val requestId: RequestId = RequestId(),
    val groupId: GroupId,
) : WebSocketMessage(kClass = GetGroupMembersRequest::class)

@Serializable
data class GetGroupMembersGroupNotFound(
    override val requestId: RequestId = RequestId(),
    val groupId: GroupId,
) : WebSocketMessage(
    kClass = GetGroupMembersGroupNotFound::class,
    description = "The group with the given id was not found",
)

@Serializable
data class GetGroupMembersUnexpectedError(
    override val requestId: RequestId = RequestId(),
    val groupId: GroupId,
) : WebSocketMessage(
    kClass = GetGroupMembersUnexpectedError::class,
    description = "An error occurred while retrieving the group members",
)

@Serializable
data class GetGroupMembersSuccessful(
    override val requestId: RequestId = RequestId(),
    val groupId: GroupId,
    val members: Set<MemberId>,
) : WebSocketMessage(
    kClass = GetGroupMembersSuccessful::class,
    description = "Successfully retrieved the group members",
)