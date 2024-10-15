package com.thebrownfoxx.neon.server.repository.message

import com.thebrownfoxx.neon.common.model.GroupId
import com.thebrownfoxx.neon.common.model.MemberId
import com.thebrownfoxx.neon.common.model.Message
import com.thebrownfoxx.neon.common.model.MessageId
import com.thebrownfoxx.neon.common.model.Result
import com.thebrownfoxx.neon.common.model.UnitResult
import com.thebrownfoxx.neon.server.repository.message.model.RepositoryAddMessageError
import com.thebrownfoxx.neon.server.repository.message.model.RepositoryGetConversationsError
import com.thebrownfoxx.neon.server.repository.message.model.RepositoryGetConversationPreviewError
import com.thebrownfoxx.neon.server.repository.message.model.RepositoryGetMessageError
import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    fun get(id: MessageId): Flow<Result<Message, RepositoryGetMessageError>>

    suspend fun add(message: Message): UnitResult<RepositoryAddMessageError>

    fun getConversations(
        memberId: MemberId,
        count: Int,
        offset: Int,
        read: Boolean,
        descending: Boolean = false,
    ): Flow<Result<Set<GroupId>, RepositoryGetConversationsError>>

    fun getConversationPreview(
        id: GroupId,
    ): Flow<Result<MessageId, RepositoryGetConversationPreviewError>>

    fun getMessages(
        groupId: GroupId,
        count: Int,
        offset: Int,
    ): Flow<Result<Set<MessageId>, RepositoryGetMessageError>>
}