package com.thebrownfoxx.neon.server.route.websocket.message

import com.thebrownfoxx.neon.common.data.websocket.model.RequestId
import com.thebrownfoxx.neon.common.data.websocket.model.WebSocketMessage
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.common.type.id.MessageId
import com.thebrownfoxx.neon.server.model.Delivery
import kotlinx.serialization.Serializable

@Serializable
data class UpdateDeliveryRequest(
    override val requestId: RequestId = RequestId(),
    val messageId: MessageId,
    val delivery: Delivery,
) : WebSocketMessage(kClass = UpdateDeliveryRequest::class)

@Serializable
data class UpdateDeliveryUnauthorized(
    override val requestId: RequestId,
    val messageId: MessageId,
    val memberId: MemberId,
) : WebSocketMessage(
    kClass = UpdateDeliveryUnauthorized::class,
    description = "The member with the given id is not authorized to update the delivery of this message",
)

@Serializable
data class UpdateDeliveryReverseDelivery(
    override val requestId: RequestId,
    val messageId: MessageId,
    val oldDelivery: Delivery,
    val newDelivery: Delivery,
) : WebSocketMessage(
    kClass = UpdateDeliveryReverseDelivery::class,
    description = "Trying to set delivery to a lesser value than the previous value",
)

@Serializable
data class UpdateDeliveryAlreadySet(
    override val requestId: RequestId,
    val messageId: MessageId,
    val delivery: Delivery,
) : WebSocketMessage(
    kClass = UpdateDeliveryAlreadySet::class,
    description = "Delivery is already set to the given value",
)

@Serializable
data class UpdateDeliveryMessageNotFound(
    override val requestId: RequestId,
    val messageId: MessageId,
) : WebSocketMessage(
    kClass = UpdateDeliveryMessageNotFound::class,
    description = "The message with the given id was not found",
)

@Serializable
data class UpdateDeliveryUnexpectedError(
    override val requestId: RequestId,
    val messageId: MessageId,
    val delivery: Delivery,
) : WebSocketMessage(
    kClass = UpdateDeliveryUnexpectedError::class,
    description = "An unexpected error occurred while updating the delivery",
)

@Serializable
data class UpdateDeliverySuccessful(
    override val requestId: RequestId,
    val messageId: MessageId,
    val delivery: Delivery,
) : WebSocketMessage(
    kClass = UpdateDeliverySuccessful::class,
    description = "The delivery was successfully updated",
)