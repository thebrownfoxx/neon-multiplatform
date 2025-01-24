package com.thebrownfoxx.neon.server.route.websocket.message

import com.thebrownfoxx.neon.common.data.websocket.model.RequestId
import com.thebrownfoxx.neon.common.data.websocket.model.WebSocketMessage
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.common.type.id.MessageId
import com.thebrownfoxx.neon.server.model.Delivery

data class GetDeliveryRequest(
    override val requestId: RequestId = RequestId(),
    val messageId: MessageId,
) : WebSocketMessage(kClass = GetDeliveryRequest::class)

data class GetDeliveryUnauthorized(
    override val requestId: RequestId = RequestId(),
    val messageId: MessageId,
    val memberId: MemberId,
) : WebSocketMessage(
    kClass = GetDeliveryUnauthorized::class,
    description = "The member with the given id is not authorized to access the message",
)

data class GetDeliveryMessageNotFound(
    override val requestId: RequestId = RequestId(),
    val messageId: MessageId,
) : WebSocketMessage(
    kClass = GetDeliveryMessageNotFound::class,
    description = "The message with the given id was not found",
)

data class GetDeliveryUnexpectedError(
    override val requestId: RequestId = RequestId(),
    val messageId: MessageId,
) : WebSocketMessage(
    kClass = GetDeliveryUnexpectedError::class,
    description = "An error occurred while retrieving the message delivery",
)

data class GetDeliverySuccessful(
    override val requestId: RequestId = RequestId(),
    val delivery: Delivery,
) : WebSocketMessage(
    kClass = GetDeliverySuccessful::class,
    description = "The message delivery was successfully retrieved",
)