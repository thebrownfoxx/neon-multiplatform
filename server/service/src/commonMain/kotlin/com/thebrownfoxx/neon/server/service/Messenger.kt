package com.thebrownfoxx.neon.server.service

import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.common.type.id.MessageId
import com.thebrownfoxx.neon.server.model.Delivery
import com.thebrownfoxx.neon.server.model.Message
import com.thebrownfoxx.neon.server.model.TimestampedMessageId
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.UnitOutcome
import kotlinx.coroutines.flow.Flow

interface Messenger {
    // TODO: Benchmark the performance of this if it would require a more modular loading solution
    fun getChatPreviews(
        actorId: MemberId,
    ): Flow<Outcome<List<Message>, GetChatPreviewsError>>

    fun getMessages(
        actorId: MemberId,
        groupId: GroupId,
    ): Flow<Outcome<List<TimestampedMessageId>, GetMessagesError>>

    fun getMessage(
        actorId: MemberId,
        id: MessageId,
    ): Flow<Outcome<Message, GetMessageError>>

    fun getDelivery(
        actorId: MemberId,
        messageId: MessageId,
    ): Flow<Outcome<Delivery, GetDeliveryError>>

    suspend fun getUnreadMessages(
        actorId: MemberId,
        messageId: MessageId,
    ): Outcome<Set<MessageId>, GetUnreadMessagesError>

    suspend fun newConversation(memberIds: Set<MemberId>): UnitOutcome<NewConversationError>

    suspend fun sendMessage(
        id: MessageId,
        actorId: MemberId,
        groupId: GroupId,
        content: String,
    ): UnitOutcome<SendMessageError>

    suspend fun updateDelivery(
        actorId: MemberId,
        messageId: MessageId,
        delivery: Delivery,
    ): UnitOutcome<UpdateDeliveryError>

    enum class GetChatPreviewsError {
        MemberNotFound,
        UnexpectedError,
    }

    enum class GetMessagesError {
        Unauthorized,
        GroupNotFound,
        UnexpectedError,
    }

    enum class GetDeliveryError {
        Unauthorized,
        MessageNotFound,
        UnexpectedError,
    }

    enum class GetMessageError {
        Unauthorized,
        NotFound,
        UnexpectedError,
    }

    enum class GetUnreadMessagesError {
        Unauthorized,
        GroupNotFound,
        UnexpectedError,
    }

    enum class NewConversationError {
        MemberNotFound,
        UnexpectedError,
    }

    enum class SendMessageError {
        Unauthorized,
        GroupNotFound,
        DuplicateId,
        UnexpectedError,
    }

    enum class MarkAsReadError {
        Unauthorized,
        AlreadyRead,
        GroupNotFound,
        UnexpectedError,
    }

    sealed interface UpdateDeliveryError {
        data object Unauthorized : UpdateDeliveryError
        data object MessageNotFound : UpdateDeliveryError
        data class ReverseDelivery(val oldDelivery: Delivery) : UpdateDeliveryError
        data object DeliveryAlreadySet : UpdateDeliveryError
        data object UnexpectedError : UpdateDeliveryError
    }
}