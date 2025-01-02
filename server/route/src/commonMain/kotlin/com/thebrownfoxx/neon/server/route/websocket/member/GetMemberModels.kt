package com.thebrownfoxx.neon.server.route.websocket.member

import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.common.websocket.model.RequestId
import com.thebrownfoxx.neon.common.websocket.model.WebSocketMessage
import com.thebrownfoxx.neon.server.model.Member
import kotlinx.serialization.Serializable

@Serializable
data class GetMemberRequest(
    override val requestId: RequestId = RequestId(),
    val id: MemberId,
) : WebSocketMessage(kClass = GetMemberRequest::class)
@Serializable
data class GetMemberNotFound(
    override val requestId: RequestId = RequestId(),
    val id: MemberId,
) : WebSocketMessage(
    kClass = GetMemberNotFound::class,
    description = "The member with the given id was not found",
)

@Serializable
data class GetMemberUnexpectedError(
    override val requestId: RequestId = RequestId(),
    val id: MemberId,
) : WebSocketMessage(
    kClass = GetMemberUnexpectedError::class,
    description = "An error occurred while retrieving the member",
)

@Serializable
data class GetMemberSuccessful(
    override val requestId: RequestId = RequestId(),
    val member: Member,
) : WebSocketMessage(
    kClass = GetMemberSuccessful::class,
    description = "Successfully retrieved the member",
)