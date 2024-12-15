package com.thebrownfoxx.neon.server.route.websocket.message

import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.common.websocket.model.WebSocketMessage
import com.thebrownfoxx.neon.server.model.TimestampedMessageId
import kotlinx.serialization.Serializable

@Serializable
data class GetMessagesRequest(
    val groupId: GroupId,
) : WebSocketMessage(kClass = GetMessagesRequest::class) {
    override val requestId = null
}

@Serializable
data class GetMessagesUnauthorized(
    val groupId: GroupId,
    val memberId: MemberId,
) : WebSocketMessage(
    kClass = GetMessagesUnauthorized::class,
    description = "The member with the given id is not authorized to access the group",
) {
    override val requestId = null
}

@Serializable
data class GetMessagesGroupNotFound(
    val groupId: GroupId,
) : WebSocketMessage(
    kClass = GetMessagesGroupNotFound::class,
    description = "The group with the given id was not found",
) {
    override val requestId = null
}

@Serializable
data class GetMessagesUnexpectedError(
    val groupId: GroupId,
) : WebSocketMessage(
    kClass = GetMessagesUnexpectedError::class,
    description = "An error occurred while retrieving the messages",
) {
    override val requestId = null
}

@Serializable
data class GetMessagesSuccessful(
    val groupId: GroupId,
    val messages: Set<TimestampedMessageId>,
) : WebSocketMessage(
    kClass = GetMessagesSuccessful::class,
    description = "Successfully retrieved the messages",
) {
    override val requestId = null
}