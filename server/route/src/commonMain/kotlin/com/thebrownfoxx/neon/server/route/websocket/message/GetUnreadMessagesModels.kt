package com.thebrownfoxx.neon.server.route.websocket.message

import com.thebrownfoxx.neon.common.data.websocket.model.RequestId
import com.thebrownfoxx.neon.common.data.websocket.model.WebSocketMessage
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.common.type.id.MessageId
import kotlinx.serialization.Serializable

@Serializable
data class GetUnreadMessagesRequest(
    override val requestId: RequestId = RequestId(),
    val groupId: GroupId,
) : WebSocketMessage(kClass = GetUnreadMessagesRequest::class)


@Serializable
data class GetUnreadMessagesUnauthorized(
    override val requestId: RequestId,
    val groupId: GroupId,
    val memberId: MemberId,
) : WebSocketMessage(
    kClass = GetUnreadMessagesUnauthorized::class,
    description = "The member with the given id is not authorized to access the group",
)

@Serializable
data class GetUnreadMessagesGroupNotFound(
    override val requestId: RequestId,
    val groupId: GroupId,
) : WebSocketMessage(
    kClass = GetUnreadMessagesGroupNotFound::class,
    description = "The group with the given id was not found",
)

@Serializable
data class GetUnreadMessagesUnexpectedError(
    override val requestId: RequestId,
    val groupId: GroupId,
    val memberId: MemberId,
) : WebSocketMessage(
    kClass = GetUnreadMessagesUnexpectedError::class,
    description = "An error occurred while retrieving the unread messages",
)

@Serializable
data class GetUnreadMessagesSuccessful(
    override val requestId: RequestId,
    val groupId: GroupId,
    val memberId: MemberId,
    val messageIds: Set<MessageId>,
) : WebSocketMessage(
    kClass = GetUnreadMessagesSuccessful::class,
    description = "Successfully retrieved the unread messages",
)