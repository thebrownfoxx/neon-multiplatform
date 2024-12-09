package com.thebrownfoxx.neon.server.service

import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.common.type.id.MessageId
import com.thebrownfoxx.neon.server.model.Message
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.UnitOutcome
import kotlinx.coroutines.flow.Flow

interface Messenger {
    fun getConversationPreviews(
        actorId: MemberId,
    ): Flow<Outcome<List<Message>, GetConversationPreviewsError>>

    fun getMessage(
        actorId: MemberId,
        id: MessageId,
    ): Flow<Outcome<Message, GetMessageError>>

    // TODO: This should return a flow
    suspend fun getMessages(
        actorId: MemberId,
        groupId: GroupId,
    ): Outcome<Set<MessageId>, GetMessagesError>

    suspend fun newConversation(memberIds: Set<MemberId>): UnitOutcome<NewConversationError>

    suspend fun sendMessage(
        actorId: MemberId,
        groupId: GroupId,
        content: String,
    ): UnitOutcome<SendMessageError>

    suspend fun markConversationAsRead(
        actorId: MemberId,
        groupId: GroupId,
    ): UnitOutcome<MarkConversationAsReadError>

    enum class GetConversationPreviewsError {
        MemberNotFound,
        UnexpectedError,
    }

    enum class GetMessageError {
        Unauthorized,
        NotFound,
        UnexpectedError,
    }

    enum class GetMessagesError {
        Unauthorized,
        GroupNotFound,
        UnexpectedError,
    }

    sealed interface NewConversationError {
        data class MemberNotFound(val memberId: MemberId) : NewConversationError
        data object UnexpectedError : NewConversationError
    }

    sealed interface SendMessageError {
        data class Unauthorized(val memberId: MemberId) : SendMessageError
        data class GroupNotFound(val groupId: GroupId) : SendMessageError
        data object UnexpectedError : SendMessageError
    }

    sealed interface MarkConversationAsReadError {
        data class Unauthorized(val memberId: MemberId) : MarkConversationAsReadError
        data object AlreadyRead : MarkConversationAsReadError
        data class GroupNotFound(val groupId: GroupId) : MarkConversationAsReadError
        data object UnexpectedError : MarkConversationAsReadError
    }
}