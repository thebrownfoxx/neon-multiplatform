package com.thebrownfoxx.neon.server.route.websocket.group

import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.websocket.model.WebSocketMessage
import com.thebrownfoxx.neon.common.websocket.model.WebSocketMessageLabel
import com.thebrownfoxx.neon.server.model.ChatGroup
import com.thebrownfoxx.neon.server.model.Community
import kotlinx.serialization.Serializable

@Serializable
data class GetGroupRequest(val id: GroupId) : WebSocketMessage(label = Label) {
    companion object {
        val Label = WebSocketMessageLabel("GetGroupRequest")
    }
}

object GetGroupResponse {
    @Serializable
    class NotFound(val id: GroupId) : WebSocketMessage(
        label = Label,
        description = "The group with the given id was not found",
    ) {
        companion object {
            val Label = WebSocketMessageLabel("GetGroupNotFound")
        }
    }

    @Serializable
    class ConnectionError(val id: GroupId) : WebSocketMessage(
        label = Label,
        description = "There was an error connecting to one of the components of the server",
    ) {
        companion object {
            val Label = WebSocketMessageLabel("GetGroupConnectionError")
        }
    }

    @Serializable
    data class SuccessfulChatGroup(val chatGroup: ChatGroup) : WebSocketMessage(
        label = Label,
        description = "Successfully retrieved the group",
    ) {
        companion object {
            val Label = WebSocketMessageLabel("GetGroupSuccessfulChatGroup")
        }
    }

    @Serializable
    data class SuccessfulCommunity(val community: Community) : WebSocketMessage(
        label = Label,
        description = "Successfully retrieved the group",
    ) {
        companion object {
            val Label = WebSocketMessageLabel("GetGroupSuccessfulCommunity")
        }
    }
}