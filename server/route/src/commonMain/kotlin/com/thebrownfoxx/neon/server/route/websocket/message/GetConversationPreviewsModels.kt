package com.thebrownfoxx.neon.server.route.websocket.message

import com.thebrownfoxx.neon.common.data.websocket.model.RequestId
import com.thebrownfoxx.neon.common.data.websocket.model.WebSocketMessage
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.server.model.Message
import kotlinx.serialization.Serializable

@Serializable
class GetChatPreviewsRequest(
    override val requestId: RequestId = RequestId(),
) : WebSocketMessage(
    kClass = GetChatPreviewsRequest::class,
)

@Serializable
data class GetChatPreviewsMemberNotFound(
    override val requestId: RequestId,
    val memberId: MemberId,
) : WebSocketMessage(
    kClass = GetChatPreviewsMemberNotFound::class,
    description = "The member with the given id was not found",
)

@Serializable
data class GetChatPreviewsUnexpectedError(
    override val requestId: RequestId,
    val memberId: MemberId,
) : WebSocketMessage(
    kClass = GetChatPreviewsUnexpectedError::class,
    description = "An error occurred while retrieving the chat previews",
)

@Serializable
data class GetChatPreviewsSuccessful(
    override val requestId: RequestId,
    val chatPreviews: List<Message>,
) : WebSocketMessage(
    kClass = GetChatPreviewsSuccessful::class,
    description = "Successfully retrieved the conversations",
)