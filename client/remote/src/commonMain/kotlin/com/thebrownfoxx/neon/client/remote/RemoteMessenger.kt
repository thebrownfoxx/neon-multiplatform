package com.thebrownfoxx.neon.client.remote

import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MessageId
import com.thebrownfoxx.neon.server.model.Message
import com.thebrownfoxx.neon.server.model.TimestampedMessageId
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.UnitOutcome
import kotlinx.coroutines.flow.Flow

interface RemoteMessenger {
    val chatPreviews: Flow<Outcome<List<Message>, GetChatPreviewsError>>

    fun getMessages(
        groupId: GroupId,
    ): Flow<Outcome<List<TimestampedMessageId>, GetMessagesError>>

    fun getMessage(id: MessageId): Flow<Outcome<Message, GetMessageError>>

    suspend fun getUnreadMessages(
        groupId: GroupId,
    ): Outcome<Set<MessageId>, GetUnreadMessagesError>

    suspend fun sendMessage(
        id: MessageId = MessageId(),
        groupId: GroupId,
        content: String,
    ): UnitOutcome<SendMessageError>

    enum class GetChatPreviewsError {
        MemberNotFound,
        UnexpectedError,
    }

    enum class GetMessagesError {
        Unauthorized,
        GroupNotFound,
        UnexpectedError,
    }

    enum class GetUnreadMessagesError {
        Unauthorized,
        GroupNotFound,
        UnexpectedError,
        RequestTimeout,
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