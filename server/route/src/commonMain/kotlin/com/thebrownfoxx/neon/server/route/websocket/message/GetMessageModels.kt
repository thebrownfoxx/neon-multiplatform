package com.thebrownfoxx.neon.server.route.websocket.message

import com.thebrownfoxx.neon.common.data.websocket.model.RequestId
import com.thebrownfoxx.neon.common.data.websocket.model.WebSocketMessage
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.common.type.id.MessageId
import com.thebrownfoxx.neon.server.model.Message
import kotlinx.serialization.Serializable

@Serializable
data class GetMessageRequest(
    override val requestId: RequestId = RequestId(),
    val id: MessageId,
) : WebSocketMessage(kClass = GetMessageRequest::class)

@Serializable
data class GetMessageUnauthorized(
    override val requestId: RequestId,
    val id: MessageId,
    val memberId: MemberId,
) : WebSocketMessage(
    kClass = GetMessageUnauthorized::class,
    description = "The member with the given id is not authorized to access the message",
)

@Serializable
data class GetMessageNotFound(
    override val requestId: RequestId,
    val id: MessageId,
    ) : WebSocketMessage(
    kClass = GetMessageNotFound::class,
    description = "The message with the given id was not found",
)

@Serializable
data class GetMessageUnexpectedError(
    override val requestId: RequestId,
    val id: MessageId,
) : WebSocketMessage(
    kClass = GetMessageUnexpectedError::class,
    description = "An error occurred while retrieving the message",
)

@Serializable
data class GetMessageSuccessful(
    override val requestId: RequestId,
    val message: Message,
) : WebSocketMessage(
    kClass = GetMessageSuccessful::class,
    description = "The message was successfully retrieved",
)