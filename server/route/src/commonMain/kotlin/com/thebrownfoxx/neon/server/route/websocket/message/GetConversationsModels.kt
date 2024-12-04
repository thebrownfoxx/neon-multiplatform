package com.thebrownfoxx.neon.server.route.websocket.message

import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.common.websocket.model.WebSocketMessage
import kotlinx.serialization.Serializable

@Serializable
class GetConversationsRequest : WebSocketMessage(
    kClass = GetConversationsRequest::class,
    description = null,
) {
    override val requestId = null
}

@Serializable
data class GetConversationsMemberNotFound(val memberId: MemberId) : WebSocketMessage(
    kClass = GetConversationsMemberNotFound::class,
    description = "The member with the given id was not found",
) {
    override val requestId = null
}

@Serializable
data class GetConversationsInternalError(val memberId: MemberId) : WebSocketMessage(
    kClass = GetConversationsInternalError::class,
    description = "An error occurred while retrieving the conversations",
) {
    override val requestId = null
}

@Serializable
data class GetConversationsSuccessful(val conversations: Set<GroupId>) : WebSocketMessage(
    kClass = GetConversationsSuccessful::class,
    description = "Successfully retrieved the conversations",
) {
    override val requestId = null
}