package com.thebrownfoxx.neon.server.route.websocket.message

import com.thebrownfoxx.neon.common.data.websocket.model.RequestId
import com.thebrownfoxx.neon.common.data.websocket.model.WebSocketMessage
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import kotlinx.serialization.Serializable

@Serializable
data class MarkConversationAsReadRequest(
    override val requestId: RequestId = RequestId(),
    val groupId: GroupId,
) : WebSocketMessage(kClass = MarkConversationAsReadRequest::class)

@Serializable
data class MarkConversationAsReadUnauthorized(
    override val requestId: RequestId,
    val groupId: GroupId,
    val memberId: MemberId,
) : WebSocketMessage(
    kClass = MarkConversationAsReadUnauthorized::class,
    description = "The member with the given id is not authorized to mark this conversation as read",
)

@Serializable
data class MarkConversationAsReadAlreadyRead(
    override val requestId: RequestId,
    val groupId: GroupId,
    val memberId: MemberId,
) : WebSocketMessage(
    kClass = MarkConversationAsReadAlreadyRead::class,
    description = "The conversation with the given id has already been marked as read",
)

@Serializable
data class MarkConversationAsReadGroupNotFound(
    override val requestId: RequestId,
    val groupId: GroupId,
) : WebSocketMessage(
    kClass = MarkConversationAsReadGroupNotFound::class,
    description = "The group with the given id was not found",
)

@Serializable
data class MarkConversationAsReadUnexpectedError(
    override val requestId: RequestId,
    val groupId: GroupId,
    val memberId: MemberId,
) : WebSocketMessage(
    kClass = MarkConversationAsReadUnexpectedError::class,
    description = "An error occurred while marking the conversation as read",
)

@Serializable
data class MarkConversationAsReadSuccessful(
    override val requestId: RequestId,
    val groupId: GroupId,
    val memberId: MemberId,
) : WebSocketMessage(
    kClass = MarkConversationAsReadSuccessful::class,
    description = "The conversation was successfully marked as read",
)