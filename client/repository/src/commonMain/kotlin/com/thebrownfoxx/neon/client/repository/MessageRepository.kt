package com.thebrownfoxx.neon.client.repository

import com.thebrownfoxx.neon.client.repository.model.AddEntityResult
import com.thebrownfoxx.neon.client.repository.model.GetEntityResult
import com.thebrownfoxx.neon.client.repository.model.UpdateEntityResult
import com.thebrownfoxx.neon.common.model.GroupId
import com.thebrownfoxx.neon.common.model.Message
import com.thebrownfoxx.neon.common.model.MessageId
import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    fun get(id: MessageId): Flow<GetEntityResult<Message>>
    suspend fun add(message: Message): AddEntityResult
    suspend fun update(message: Message): UpdateEntityResult

    fun getConversations(
        count: Int,
        offset: Int,
        read: Boolean,
        descending: Boolean = false,
    ): Flow<GetEntityResult<List<GroupId>>>

    fun getConversationPreview(id: GroupId): Flow<GetEntityResult<MessageId?>>
    fun getMessages(groupId: GroupId): Flow<GetEntityResult<List<MessageId>>>
}