package com.thebrownfoxx.neon.client.service

import com.thebrownfoxx.neon.client.model.LocalChatPreviews
import com.thebrownfoxx.neon.client.model.LocalMessage
import com.thebrownfoxx.neon.client.model.LocalTimestampedMessageId
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.common.type.id.MessageId
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.UnitOutcome
import kotlinx.coroutines.flow.Flow

interface Messenger {
    val chatPreviews:
            Flow<Outcome<LocalChatPreviews, GetChatPreviewsError>>

    fun getMessages(
        groupId: GroupId,
    ): Flow<Outcome<List<LocalTimestampedMessageId>, GetMessagesError>>

    fun getMessage(id: MessageId): Flow<Outcome<LocalMessage, GetMessageError>>

    suspend fun sendMessage(
        id: MessageId = MessageId(),
        groupId: GroupId,
        content: String,
    ): UnitOutcome<SendMessageError>

    suspend fun markConversationAsRead(groupId: GroupId): UnitOutcome<MarkConversationAsReadError>

    enum class GetChatPreviewsError {
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

    enum class SendMessageError {
        Unauthorized,
        GroupNotFound,
        DuplicateId,
        UnexpectedError,
        RequestTimeout,
    }

    enum class MarkConversationAsReadError {
        Unauthorized,
        AlreadyRead,
        GroupNotFound,
        UnexpectedError,
        RequestTimeout,
    }
}

fun List<LocalMessage>.toChatPreviews(
    loggedInMemberId: MemberId,
): LocalChatPreviews {
    val (allUnreadPreviews, readPreviews) = partition { message ->
        message.senderId != loggedInMemberId || message.delivery != com.thebrownfoxx.neon.client.model.LocalDelivery.Delivered
    }
    val unreadLarge = allUnreadPreviews.size > 10
    val nudgedPreviews = when {
        unreadLarge -> allUnreadPreviews.takeLast(2)
        else -> emptyList()
    }
    val unreadPreviews = when {
        unreadLarge -> allUnreadPreviews.dropLast(2)
        else -> allUnreadPreviews
    }

    return LocalChatPreviews(
        nudgedPreviews = nudgedPreviews,
        unreadPreviews = unreadPreviews,
        readPreviews = readPreviews,
    )
}