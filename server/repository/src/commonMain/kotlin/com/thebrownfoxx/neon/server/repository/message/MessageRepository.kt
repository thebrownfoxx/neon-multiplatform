package com.thebrownfoxx.neon.server.repository.message

import com.thebrownfoxx.neon.common.model.GroupId
import com.thebrownfoxx.neon.common.model.MemberId
import com.thebrownfoxx.neon.common.model.Message
import com.thebrownfoxx.neon.common.model.MessageId
import com.thebrownfoxx.neon.common.model.Result
import com.thebrownfoxx.neon.common.model.UnitResult
import com.thebrownfoxx.neon.server.repository.message.model.AddMessageError
import com.thebrownfoxx.neon.server.repository.message.model.GetConversationsError
import com.thebrownfoxx.neon.server.repository.message.model.GetConversationPreviewError
import com.thebrownfoxx.neon.server.repository.message.model.GetMessageError
import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    fun get(id: MessageId): Flow<Result<Message, GetMessageError>>

    suspend fun add(message: Message): UnitResult<AddMessageError>

    fun getConversations(
        memberId: MemberId,
        count: Int,
        offset: Int,
        read: Boolean,
        descending: Boolean = false,
    ): Flow<Result<Set<GroupId>, GetConversationsError>>

    fun getConversationPreview(
        id: GroupId,
    ): Flow<Result<MessageId, GetConversationPreviewError>>

    fun getMessages(
        groupId: GroupId,
        count: Int,
        offset: Int,
    ): Flow<Result<Set<MessageId>, GetMessageError>>
}