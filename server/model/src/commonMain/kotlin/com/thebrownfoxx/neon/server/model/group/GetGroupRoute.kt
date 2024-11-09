package com.thebrownfoxx.neon.server.model.group

import com.thebrownfoxx.neon.common.model.ChatGroup
import com.thebrownfoxx.neon.common.model.Community
import com.thebrownfoxx.neon.server.model.Response
import kotlinx.serialization.Serializable

object GetGroupResponse {
    enum class Status {
        NotFound,
        InternalConnectionError,
        Successful,
    }

    @Serializable
    class NotFound : Response(
        status = Status.NotFound.name,
        description = "The group with the given id was not found",
    )

    @Serializable
    class ConnectionError : Response(
        status = Status.InternalConnectionError.name,
        description = "There was an error connecting to one of the components of the server",
    )

    @Serializable
    data class SuccessfulChatGroup(val chatGroup: ChatGroup) : Response(
        status = Status.Successful.name,
        description = "Successfully retrieved the group",
    )

    @Serializable
    data class SuccessfulCommunity(val community: Community) : Response(
        status = Status.Successful.name,
        description = "Successfully retrieved the group",
    )
}