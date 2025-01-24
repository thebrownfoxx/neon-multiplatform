package com.thebrownfoxx.neon.server.route.websocket.message

import com.thebrownfoxx.neon.common.data.websocket.model.RequestId
import com.thebrownfoxx.neon.common.data.websocket.model.WebSocketMessage
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import kotlinx.serialization.Serializable

@Deprecated("Use UpdateDelivery instead")
@Serializable
data class MarkAsReadRequest(
    override val requestId: RequestId = RequestId(),
    val groupId: GroupId,
) : WebSocketMessage(kClass = MarkAsReadRequest::class)

@Deprecated("Use UpdateDelivery instead")
@Serializable
data class MarkAsReadUnauthorized(
    override val requestId: RequestId,
    val groupId: GroupId,
    val memberId: MemberId,
) : WebSocketMessage(
    kClass = MarkAsReadUnauthorized::class,
    description = "The member with the given id is not authorized to mark this conversation as read",
)

@Deprecated("Use UpdateDelivery instead")
@Serializable
data class MarkAsReadAlreadyRead(
    override val requestId: RequestId,
    val groupId: GroupId,
    val memberId: MemberId,
) : WebSocketMessage(
    kClass = MarkAsReadAlreadyRead::class,
    description = "The conversation with the given id has already been marked as read",
)

@Deprecated("Use UpdateDelivery instead")
@Serializable
data class MarkAsReadGroupNotFound(
    override val requestId: RequestId,
    val groupId: GroupId,
) : WebSocketMessage(
    kClass = MarkAsReadGroupNotFound::class,
    description = "The group with the given id was not found",
)

@Deprecated("Use UpdateDelivery instead")
@Serializable
data class MarkAsReadUnexpectedError(
    override val requestId: RequestId,
    val groupId: GroupId,
    val memberId: MemberId,
) : WebSocketMessage(
    kClass = MarkAsReadUnexpectedError::class,
    description = "An error occurred while marking the conversation as read",
)

@Deprecated("Use UpdateDelivery instead")
@Serializable
data class MarkAsReadSuccessful(
    override val requestId: RequestId,
    val groupId: GroupId,
    val memberId: MemberId,
) : WebSocketMessage(
    kClass = MarkAsReadSuccessful::class,
    description = "The conversation was successfully marked as read",
)