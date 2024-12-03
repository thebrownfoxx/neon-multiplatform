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
) : WebSocketMessage(kClass = this::class)

@Serializable
data class GetMemberNotFound(
    override val requestId: RequestId = RequestId(),
    val id: MemberId,
) : WebSocketMessage(
    kClass = GetMemberNotFound::class,
    description = "The member with the given id was not found",
)

@Serializable
data class GetMemberConnectionError(
    override val requestId: RequestId = RequestId(),
    val id: MemberId,
) : WebSocketMessage(
    kClass = GetMemberConnectionError::class,
    description = "There was an error connecting to one of the components of the server",
)

@Serializable
data class GetMemberSuccessful(
    override val requestId: RequestId = RequestId(),
    val member: Member,
) : WebSocketMessage(
    kClass = GetMemberSuccessful::class,
    description = "Successfully retrieved the member",
)