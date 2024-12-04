package com.thebrownfoxx.neon.server.route.websocket.group

import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.websocket.model.WebSocketMessage
import com.thebrownfoxx.neon.server.model.ChatGroup
import com.thebrownfoxx.neon.server.model.Community
import kotlinx.serialization.Serializable

@Serializable
data class GetGroupRequest(
    val id: GroupId,
) : WebSocketMessage(kClass = GetGroupRequest::class) {
    override val requestId = null
}

@Serializable
data class GetGroupNotFound(
    val id: GroupId,
) : WebSocketMessage(
    kClass = GetGroupNotFound::class,
    description = "The group with the given id was not found",
) {
    override val requestId = null
}

@Serializable
data class GetGroupInternalError(
    val id: GroupId,
) : WebSocketMessage(
    kClass = GetGroupInternalError::class,
    description = "An error occurred while retrieving the group",
) {
    override val requestId = null
}

@Serializable
data class GetGroupSuccessfulChatGroup(
    val chatGroup: ChatGroup,
) : WebSocketMessage(
    kClass = GetGroupSuccessfulChatGroup::class,
    description = "Successfully retrieved the group",
) {
    override val requestId = null
}

@Serializable
data class GetGroupSuccessfulCommunity(
    val community: Community,
) : WebSocketMessage(
    kClass = GetGroupSuccessfulCommunity::class,
    description = "Successfully retrieved the group",
) {
    override val requestId = null
}