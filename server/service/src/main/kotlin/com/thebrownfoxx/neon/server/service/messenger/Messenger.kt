package com.thebrownfoxx.neon.server.service.messenger

import com.thebrownfoxx.neon.common.outcome.Outcome
import com.thebrownfoxx.neon.common.outcome.UnitOutcome
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.common.type.id.MessageId
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
    @Deprecated("Use getConversations(MemberId) instead")
    suspend fun getConversations(
        actorId: MemberId,
        count: Int,
        offset: Int,
    ): Outcome<Conversations, GetConversationsError>

    suspend fun getConversations(actorId: MemberId): Outcome<Conversations, GetConversationsError>

    fun getMessage(
        actorId: MemberId,
        id: MessageId,
    ): Flow<Outcome<Message, GetMessageError>>

    fun getConversationPreview(
        actorId: MemberId,
        groupId: GroupId,
    ): Flow<Outcome<MessageId?, GetConversationPreviewError>>

    @Deprecated("Use getMessages(MemberId, GroupId) instead")
    suspend fun getMessages(
        actorId: MemberId,
        groupId: GroupId,
        count: Int,
        offset: Int,
    ): Outcome<Set<MessageId>, GetMessagesError>

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
}