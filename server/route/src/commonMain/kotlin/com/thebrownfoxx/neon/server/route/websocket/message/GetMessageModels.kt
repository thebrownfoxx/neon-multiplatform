package com.thebrownfoxx.neon.server.route.websocket.message

import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.common.type.id.MessageId
import com.thebrownfoxx.neon.common.websocket.model.WebSocketMessage
import com.thebrownfoxx.neon.server.model.Message
import kotlinx.serialization.Serializable

@Serializable
data class GetMessageRequest(val id: MessageId) : WebSocketMessage(
    kClass = GetMessageRequest::class,
    description = null,
) {
    override val requestId = null
}

@Serializable
data class GetMessageUnauthorized(
    val id: MessageId,
    val memberId: MemberId,
) : WebSocketMessage(
    kClass = GetMessageUnauthorized::class,
    description = "The member with the given id is not authorized to access the group",
) {
    override val requestId = null
}

@Serializable
data class GetMessageNotFound(val id: MessageId) : WebSocketMessage(
    kClass = GetMessageNotFound::class,
    description = "The group with the given id was not found",
) {
    override val requestId = null
}

@Serializable
data class GetMessageUnexpectedError(val id: MessageId) : WebSocketMessage(
    kClass = GetMessageUnexpectedError::class,
    description = "An error occurred while retrieving the message",
) {
    override val requestId = null
}

@Serializable
data class GetMessageSuccessful(val message: Message) : WebSocketMessage(
    kClass = GetMessageSuccessful::class,
    description = "The message was successfully retrieved",
) {
    override val requestId = null
}