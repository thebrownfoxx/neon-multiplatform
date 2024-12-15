package com.thebrownfoxx.neon.server.service

import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.common.type.id.MessageId
import com.thebrownfoxx.neon.server.model.Message
import com.thebrownfoxx.neon.server.model.TimestampedMessageId
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.UnitOutcome
import kotlinx.coroutines.flow.Flow

interface Messenger {
    // TODO: Benchmark the performance of this if it would require a more modular loading solution
    fun getConversationPreviews(
        actorId: MemberId,
    ): Flow<Outcome<List<Message>, GetConversationPreviewsError>>

    fun getMessages(
        actorId: MemberId,
        groupId: GroupId,
    ): Flow<Outcome<Set<TimestampedMessageId>, GetMessagesError>>

    fun getMessage(
        actorId: MemberId,
        id: MessageId,
    ): Flow<Outcome<Message, GetMessageError>>

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

    enum class GetMessagesError {
        Unauthorized,
        GroupNotFound,
        UnexpectedError,
    }

    enum class GetMessageError {
        Unauthorized,
        NotFound,
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