package com.thebrownfoxx.neon.server.route.websocket.group

import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.common.websocket.model.WebSocketMessage
import kotlinx.serialization.Serializable

@Serializable
data class GetGroupMembersRequest(
    val groupId: GroupId,
) : WebSocketMessage(kClass = GetGroupMembersRequest::class) {
    override val requestId = null
}

@Serializable
data class GetGroupMembersGroupNotFound(
    val groupId: GroupId,
) : WebSocketMessage(
    kClass = GetGroupMembersGroupNotFound::class,
    description = "The group with the given id was not found",
) {
    override val requestId = null
}

@Serializable
data class GetGroupMembersUnexpectedError(
    val groupId: GroupId,
) : WebSocketMessage(
    kClass = GetGroupMembersUnexpectedError::class,
    description = "An error occurred while retrieving the group members",
) {
    override val requestId = null
}

@Serializable
data class GetGroupMembersSuccessful(
    val groupId: GroupId,
    val members: Set<MemberId>,
) : WebSocketMessage(
    kClass = GetGroupMembersSuccessful::class,
    description = "Successfully retrieved the group members",
) {
    override val requestId = null
}