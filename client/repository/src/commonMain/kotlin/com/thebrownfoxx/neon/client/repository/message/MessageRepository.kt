package com.thebrownfoxx.neon.client.repository.message

import com.thebrownfoxx.neon.client.repository.message.model.AddMessageEntityError
import com.thebrownfoxx.neon.client.repository.message.model.GetConversationPreviewEntityError
import com.thebrownfoxx.neon.client.repository.message.model.GetConversationEntitiesError
import com.thebrownfoxx.neon.client.repository.message.model.GetMessageEntityError
import com.thebrownfoxx.neon.common.model.GroupId
import com.thebrownfoxx.neon.common.model.MemberId
import com.thebrownfoxx.neon.common.model.Message
import com.thebrownfoxx.neon.common.model.MessageId
import com.thebrownfoxx.neon.common.model.Result
import com.thebrownfoxx.neon.common.model.UnitResult
import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    fun get(id: MessageId): Flow<Result<Message, GetMessageEntityError>>

    suspend fun add(message: Message): UnitResult<AddMessageEntityError>

    fun getConversations(
        memberId: MemberId,
        count: Int,
        offset: Int,
        read: Boolean,
        descending: Boolean = false,
    ): Flow<Result<Set<GroupId>, GetConversationEntitiesError>>

    fun getConversationPreview(id: GroupId): Flow<Result<MessageId, GetConversationPreviewEntityError>>

    fun getMessages(
        groupId: GroupId,
        count: Int,
        offset: Int,
    ): Flow<Result<Set<MessageId>, GetMessageEntityError>>
}