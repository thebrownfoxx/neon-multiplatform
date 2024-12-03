package com.thebrownfoxx.neon.server.route.websocket.group

import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.websocket.model.WebSocketMessage
import com.thebrownfoxx.neon.server.model.ChatGroup
import com.thebrownfoxx.neon.server.model.Community
import kotlinx.serialization.Serializable

@Serializable
data class GetGroupRequest(val id: GroupId) : WebSocketMessage(kClass = this::class)

object GetGroupResponse {
    @Serializable
    class NotFound(val id: GroupId) : WebSocketMessage(
        kClass = this::class,
        description = "The group with the given id was not found",
    )

    @Serializable
    class ConnectionError(val id: GroupId) : WebSocketMessage(
        kClass = this::class,
        description = "There was an error connecting to one of the components of the server",
    )

    @Serializable
    data class SuccessfulChatGroup(val chatGroup: ChatGroup) : WebSocketMessage(
        kClass = this::class,
        description = "Successfully retrieved the group",
    )

    @Serializable
    data class SuccessfulCommunity(val community: Community) : WebSocketMessage(
        kClass = this::class,
        description = "Successfully retrieved the group",
    )
}