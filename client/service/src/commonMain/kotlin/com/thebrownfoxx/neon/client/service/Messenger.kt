package com.thebrownfoxx.neon.client.service

import com.thebrownfoxx.neon.client.model.LocalConversationPreviews
import com.thebrownfoxx.neon.client.model.LocalMessage
import com.thebrownfoxx.neon.client.model.LocalTimestampedMessageId
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.common.type.id.MessageId
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.UnitOutcome
import kotlinx.coroutines.flow.Flow

interface Messenger {
    val conversationPreviews:
            Flow<Outcome<LocalConversationPreviews, GetConversationPreviewsError>>

    fun getMessages(
        groupId: GroupId,
    ): Flow<Outcome<List<LocalTimestampedMessageId>, GetMessagesError>>

    fun getMessage(id: MessageId): Flow<Outcome<LocalMessage, GetMessageError>>

    suspend fun sendMessage(
        id: MessageId = MessageId(),
        groupId: GroupId,
        content: String,
    ): UnitOutcome<SendMessageError>

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

    enum class SendMessageError {
        Unauthorized,
        GroupNotFound,
        DuplicateId,
        UnexpectedError,
        RequestTimeout,
    }
}

fun List<LocalMessage>.toConversationPreviews(
    loggedInMemberId: MemberId,
): LocalConversationPreviews {
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

    return LocalConversationPreviews(
        nudgedPreviews = nudgedPreviews,
        unreadPreviews = unreadPreviews,
        readPreviews = readPreviews,
    )
}