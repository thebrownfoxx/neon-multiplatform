package com.thebrownfoxx.neon.server.route.websocket.message

import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.common.websocket.model.WebSocketMessage
import com.thebrownfoxx.neon.server.model.Message
import kotlinx.serialization.Serializable

@Serializable
class GetConversationPreviewsRequest : WebSocketMessage(
    kClass = GetConversationPreviewsRequest::class,
) {
    override val requestId = null
}

@Serializable
data class GetConversationPreviewsMemberNotFound(val memberId: MemberId) : WebSocketMessage(
    kClass = GetConversationPreviewsMemberNotFound::class,
    description = "The member with the given id was not found",
) {
    override val requestId = null
}

@Serializable
data class GetConversationPreviewsUnexpectedError(val memberId: MemberId) : WebSocketMessage(
    kClass = GetConversationPreviewsUnexpectedError::class,
    description = "An error occurred while retrieving the conversation previews",
) {
    override val requestId = null
}

@Serializable
data class GetConversationPreviewsSuccessful(
    val conversationPreviews: List<Message>,
) : WebSocketMessage(
    kClass = GetConversationPreviewsSuccessful::class,
    description = "Successfully retrieved the conversations",
) {
    override val requestId = null
}