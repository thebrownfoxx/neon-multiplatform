package com.thebrownfoxx.neon.server.service.messenger

import com.thebrownfoxx.neon.common.model.GroupId
import com.thebrownfoxx.neon.common.model.MemberId
import com.thebrownfoxx.neon.common.model.MessageId
import com.thebrownfoxx.neon.common.model.Result
import com.thebrownfoxx.neon.common.model.UnitResult
import com.thebrownfoxx.neon.server.model.Message
import com.thebrownfoxx.neon.server.service.messenger.model.Conversations
import com.thebrownfoxx.neon.server.service.messenger.model.GetConversationPreviewError
import com.thebrownfoxx.neon.server.service.messenger.model.GetConversationsError
import com.thebrownfoxx.neon.server.service.messenger.model.GetMessageError
import com.thebrownfoxx.neon.server.service.messenger.model.GetMessagesError
import com.thebrownfoxx.neon.server.service.messenger.model.MarkConversationAsReadError
import com.thebrownfoxx.neon.server.service.messenger.model.NewConversationError
import com.thebrownfoxx.neon.server.service.messenger.model.SendMessageError
import kotlinx.coroutines.flow.Flow

interface Messenger {
    suspend fun getConversations(
        actorId: MemberId,
        count: Int,
        offset: Int,
    ): Result<Conversations, GetConversationsError>

    fun getMessage(
        actorId: MemberId,
        id: MessageId,
    ): Flow<Result<Message, GetMessageError>>

    fun getConversationPreview(
        actorId: MemberId,
        groupId: GroupId,
    ): Flow<Result<MessageId?, GetConversationPreviewError>>

    suspend fun getMessages(
        actorId: MemberId,
        groupId: GroupId,
        count: Int,
        offset: Int,
    ): Result<Set<MessageId>, GetMessagesError>

    suspend fun newConversation(memberIds: Set<MemberId>): UnitResult<NewConversationError>

    suspend fun sendMessage(
        actorId: MemberId,
        groupId: GroupId,
        content: String,
    ): UnitResult<SendMessageError>

    suspend fun markConversationAsRead(
        actorId: MemberId,
        groupId: GroupId,
    ): UnitResult<MarkConversationAsReadError>
}