package com.thebrownfoxx.neon.server.repository.message

import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.common.type.id.MessageId
import com.thebrownfoxx.neon.common.type.Outcome
import com.thebrownfoxx.neon.common.type.UnitOutcome
import com.thebrownfoxx.neon.server.model.Message
import com.thebrownfoxx.neon.server.repository.message.model.RepositoryAddMessageError
import com.thebrownfoxx.neon.server.repository.message.model.RepositoryGetConversationCountError
import com.thebrownfoxx.neon.server.repository.message.model.RepositoryGetConversationPreviewError
import com.thebrownfoxx.neon.server.repository.message.model.RepositoryGetConversationsError
import com.thebrownfoxx.neon.server.repository.message.model.RepositoryGetMessageError
import com.thebrownfoxx.neon.server.repository.message.model.RepositoryGetMessagesError
import com.thebrownfoxx.neon.server.repository.message.model.RepositoryUpdateMessageError
import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    fun get(id: MessageId): Flow<Outcome<Message, RepositoryGetMessageError>>

    suspend fun add(message: Message): UnitOutcome<RepositoryAddMessageError>

    suspend fun update(message: Message): UnitOutcome<RepositoryUpdateMessageError>

    suspend fun getConversations(
        memberId: MemberId,
        count: Int,
        offset: Int,
        read: Boolean? = null,
        descending: Boolean = false,
    ): Outcome<Set<GroupId>, RepositoryGetConversationsError>

    fun getConversationCount(
        memberId: MemberId,
        read: Boolean? = null,
    ): Flow<Outcome<Int, RepositoryGetConversationCountError>>

    fun getConversationPreview(
        id: GroupId,
    ): Flow<Outcome<MessageId?, RepositoryGetConversationPreviewError>>

    fun getMessages(
        groupId: GroupId,
        count: Int,
        offset: Int,
    ): Flow<Outcome<Set<MessageId>, RepositoryGetMessagesError>>

    fun getUnreadMessages(
        groupId: GroupId,
    ): Flow<Outcome<Set<MessageId>, RepositoryGetMessagesError>>
}