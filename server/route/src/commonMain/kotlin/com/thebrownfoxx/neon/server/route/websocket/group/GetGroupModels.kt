package com.thebrownfoxx.neon.server.route.websocket.group

import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.websocket.model.WebSocketMessage
import com.thebrownfoxx.neon.server.model.ChatGroup
import com.thebrownfoxx.neon.server.model.Community
import kotlinx.serialization.Serializable

@Serializable
data class GetGroupRequest(val id: GroupId) : WebSocketMessage(kClass = GetGroupRequest::class)

@Serializable
data class GetGroupNotFound(val id: GroupId) : WebSocketMessage(
    kClass = GetGroupNotFound::class,
    description = "The group with the given id was not found",
)

@Serializable
data class GetGroupConnectionError(val id: GroupId) : WebSocketMessage(
    kClass = GetGroupConnectionError::class,
    description = "There was an error connecting to one of the components of the server",
)

@Serializable
data class GetGroupSuccessfulChatGroup(val chatGroup: ChatGroup) : WebSocketMessage(
    kClass = GetGroupSuccessfulChatGroup::class,
    description = "Successfully retrieved the group",
)

@Serializable
data class GetGroupSuccessfulCommunity(val community: Community) : WebSocketMessage(
    kClass = GetGroupSuccessfulCommunity::class,
    description = "Successfully retrieved the group",
)