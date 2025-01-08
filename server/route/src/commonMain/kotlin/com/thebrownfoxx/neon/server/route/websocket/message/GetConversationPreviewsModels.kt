package com.thebrownfoxx.neon.server.route.websocket.message

import com.thebrownfoxx.neon.common.data.websocket.model.RequestId
import com.thebrownfoxx.neon.common.data.websocket.model.WebSocketMessage
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.server.model.Message
import kotlinx.serialization.Serializable

@Serializable
class GetConversationPreviewsRequest(
    override val requestId: RequestId = RequestId(),
) : WebSocketMessage(
    kClass = GetConversationPreviewsRequest::class,
)

@Serializable
data class GetConversationPreviewsMemberNotFound(
    override val requestId: RequestId,
    val memberId: MemberId,
) : WebSocketMessage(
    kClass = GetConversationPreviewsMemberNotFound::class,
    description = "The member with the given id was not found",
)

@Serializable
data class GetConversationPreviewsUnexpectedError(
    override val requestId: RequestId,
    val memberId: MemberId,
) : WebSocketMessage(
    kClass = GetConversationPreviewsUnexpectedError::class,
    description = "An error occurred while retrieving the conversation previews",
)

@Serializable
data class GetConversationPreviewsSuccessful(
    override val requestId: RequestId,
    val conversationPreviews: List<Message>,
) : WebSocketMessage(
    kClass = GetConversationPreviewsSuccessful::class,
    description = "Successfully retrieved the conversations",
)