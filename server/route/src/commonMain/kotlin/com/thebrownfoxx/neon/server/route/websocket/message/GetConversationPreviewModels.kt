package com.thebrownfoxx.neon.server.route.websocket.message

import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.common.type.id.MessageId
import com.thebrownfoxx.neon.common.websocket.model.WebSocketMessage
import kotlinx.serialization.Serializable

@Serializable
data class GetConversationPreviewRequest(
    val groupId: GroupId,
) : WebSocketMessage(kClass = GetConversationPreviewRequest::class) {
    override val requestId = null
}

@Serializable
data class GetConversationPreviewUnauthorized(
    val groupId: GroupId,
    val memberId: MemberId,
) : WebSocketMessage(
    kClass = GetConversationPreviewUnauthorized::class,
    description = "The member with the given id is not authorized to access the group",
) {
    override val requestId = null
}

@Serializable
data class GetConversationPreviewGroupNotFound(
    val groupId: GroupId,
) : WebSocketMessage(
    kClass = GetConversationPreviewGroupNotFound::class,
    description = "The group with the given id was not found",
) {
    override val requestId = null
}

@Serializable
data class GetConversationPreviewUnexpectedError(
    val groupId: GroupId,
) : WebSocketMessage(
    kClass = GetConversationPreviewUnexpectedError::class,
    description = "An error occurred while retrieving the conversation preview",
) {
    override val requestId = null
}

@Serializable
data class GetConversationPreviewSuccessful(
    val groupId: GroupId,
    val previewId: MessageId?,
) : WebSocketMessage(
    kClass = GetConversationPreviewSuccessful::class,
    description = "The conversation preview was successfully retrieved",
) {
    override val requestId = null
}