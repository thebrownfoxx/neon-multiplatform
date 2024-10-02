package com.thebrownfoxx.neon.client.service

import com.thebrownfoxx.neon.client.service.model.Conversations
import com.thebrownfoxx.neon.common.model.GroupId
import com.thebrownfoxx.neon.common.model.Message
import com.thebrownfoxx.neon.common.model.MessageId
import kotlinx.coroutines.flow.Flow

interface Messenger {
    val conversations: Flow<Conversations>

    fun get(id: MessageId): Flow<Message>
    fun getConversationPreview(id: GroupId): Flow<MessageId>
    fun getMessages(groupId: GroupId): Flow<MessageId>
    suspend fun send(message: Message)
    suspend fun markAsRead(groupId: GroupId)
}