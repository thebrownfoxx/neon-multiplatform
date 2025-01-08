package com.thebrownfoxx.neon.server.route.websocket.group

import com.thebrownfoxx.neon.common.data.websocket.model.RequestId
import com.thebrownfoxx.neon.common.data.websocket.model.WebSocketMessage
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.server.model.ChatGroup
import com.thebrownfoxx.neon.server.model.Community
import kotlinx.serialization.Serializable

@Serializable
data class GetGroupRequest(
    override val requestId: RequestId = RequestId(),
    val id: GroupId,
) : WebSocketMessage(kClass = GetGroupRequest::class)

@Serializable
data class GetGroupNotFound(
    override val requestId: RequestId = RequestId(),
    val id: GroupId,
) : WebSocketMessage(
    kClass = GetGroupNotFound::class,
    description = "The group with the given id was not found",
)

@Serializable
data class GetGroupUnexpectedError(
    override val requestId: RequestId = RequestId(),
    val id: GroupId,
) : WebSocketMessage(
    kClass = GetGroupUnexpectedError::class,
    description = "An error occurred while retrieving the group",
)

@Serializable
data class GetGroupSuccessfulChatGroup(
    override val requestId: RequestId = RequestId(),
    val chatGroup: ChatGroup,
) : WebSocketMessage(
    kClass = GetGroupSuccessfulChatGroup::class,
    description = "Successfully retrieved the group",
)

@Serializable
data class GetGroupSuccessfulCommunity(
    override val requestId: RequestId = RequestId(),
    val community: Community,
) : WebSocketMessage(
    kClass = GetGroupSuccessfulCommunity::class,
    description = "Successfully retrieved the group",
)