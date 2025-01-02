package com.thebrownfoxx.neon.server.route.websocket.message

import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.common.websocket.model.RequestId
import com.thebrownfoxx.neon.common.websocket.model.WebSocketMessage
import com.thebrownfoxx.neon.server.model.TimestampedMessageId
import kotlinx.serialization.Serializable

@Serializable
data class GetMessagesRequest(
    override val requestId: RequestId = RequestId(),
    val groupId: GroupId,
) : WebSocketMessage(kClass = GetMessagesRequest::class)

@Serializable
data class GetMessagesUnauthorized(
    override val requestId: RequestId,
    val groupId: GroupId,
    val memberId: MemberId,
) : WebSocketMessage(
    kClass = GetMessagesUnauthorized::class,
    description = "The member with the given id is not authorized to access the group",
)

@Serializable
data class GetMessagesGroupNotFound(
    override val requestId: RequestId,
    val groupId: GroupId,
) : WebSocketMessage(
    kClass = GetMessagesGroupNotFound::class,
    description = "The group with the given id was not found",
)

@Serializable
data class GetMessagesUnexpectedError(
    override val requestId: RequestId,
    val groupId: GroupId,
) : WebSocketMessage(
    kClass = GetMessagesUnexpectedError::class,
    description = "An error occurred while retrieving the messages",
)

@Serializable
data class GetMessagesSuccessful(
    override val requestId: RequestId,
    val groupId: GroupId,
    val messages: Set<TimestampedMessageId>,
) : WebSocketMessage(
    kClass = GetMessagesSuccessful::class,
    description = "Successfully retrieved the messages",
)