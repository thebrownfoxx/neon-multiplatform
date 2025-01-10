package com.thebrownfoxx.neon.server.route.websocket.message

import com.thebrownfoxx.neon.common.data.websocket.model.RequestId
import com.thebrownfoxx.neon.common.data.websocket.model.WebSocketMessage
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.common.type.id.MessageId
import kotlinx.serialization.Serializable

@Serializable
data class SendMessageRequest(
    override val requestId: RequestId = RequestId(),
    val id: MessageId,
    val groupId: GroupId,
    val content: String,
) : WebSocketMessage(kClass = SendMessageRequest::class)

@Serializable
data class SendMessageUnauthorized(
    override val requestId: RequestId,
    val id: MessageId,
    val memberId: MemberId,
) : WebSocketMessage(kClass = SendMessageUnauthorized::class)

@Serializable
data class SendMessageGroupNotFound(
    override val requestId: RequestId,
    val id: MessageId,
    val groupId: GroupId,
) : WebSocketMessage(kClass = SendMessageGroupNotFound::class)

@Serializable
data class SendMessageDuplicateId(
    override val requestId: RequestId,
    val id: MessageId,
) : WebSocketMessage(kClass = SendMessageDuplicateId::class)

@Serializable
data class SendMessageUnexpectedError(
    override val requestId: RequestId,
    val id: MessageId,
) : WebSocketMessage(kClass = SendMessageUnexpectedError::class)

@Serializable
data class SendMessageSuccessful(
    override val requestId: RequestId,
    val id: MessageId,
) : WebSocketMessage(kClass = SendMessageSuccessful::class)