package com.thebrownfoxx.neon.server.repository.message

import com.thebrownfoxx.neon.common.model.GroupId
import com.thebrownfoxx.neon.common.model.MemberId
import com.thebrownfoxx.neon.common.model.MessageId
import com.thebrownfoxx.neon.common.model.Result
import com.thebrownfoxx.neon.common.model.UnitResult
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
    fun get(id: MessageId): Flow<Result<Message, RepositoryGetMessageError>>

    suspend fun add(message: Message): UnitResult<RepositoryAddMessageError>

    suspend fun update(message: Message): UnitResult<RepositoryUpdateMessageError>

    fun getConversations(
        memberId: MemberId,
        count: Int,
        offset: Int,
        read: Boolean? = null,
        descending: Boolean = false,
    ): Flow<Result<Set<GroupId>, RepositoryGetConversationsError>>

    fun getConversationCount(
        memberId: MemberId,
        read: Boolean? = null,
    ): Flow<Result<Int, RepositoryGetConversationCountError>>

    fun getConversationPreview(
        id: GroupId,
    ): Flow<Result<MessageId?, RepositoryGetConversationPreviewError>>

    fun getMessages(
        groupId: GroupId,
        count: Int,
        offset: Int,
    ): Flow<Result<Set<MessageId>, RepositoryGetMessagesError>>

    fun getUnreadMessages(
        groupId: GroupId,
    ): Flow<Result<Set<MessageId>, RepositoryGetMessagesError>>
}