package com.thebrownfoxx.neon.client.service.messenger

import com.thebrownfoxx.neon.client.service.messenger.model.Conversations
import com.thebrownfoxx.neon.client.service.messenger.model.GetMessageError
import com.thebrownfoxx.neon.client.service.messenger.model.GetMessagesError
import com.thebrownfoxx.neon.client.service.messenger.model.MarkConversationAsReadError
import com.thebrownfoxx.neon.client.service.messenger.model.SendMessageError
import com.thebrownfoxx.neon.common.model.GroupId
import com.thebrownfoxx.neon.common.model.Message
import com.thebrownfoxx.neon.common.model.MessageId
import com.thebrownfoxx.neon.common.model.Result
import com.thebrownfoxx.neon.common.model.UnitResult
import kotlinx.coroutines.flow.Flow

interface Messenger {
    val conversations: Flow<Conversations>

    fun getMessage(id: MessageId): Flow<Result<Message, GetMessageError>>
    fun getConversationPreview(id: GroupId): Flow<Result<MessageId?, GetMessageError>>
    fun getMessages(groupId: GroupId): Flow<Result<List<MessageId>, GetMessagesError>>
    suspend fun sendMessage(groupId: GroupId, content: String) : UnitResult<SendMessageError>
    suspend fun markConversationAsRead(groupId: GroupId) : UnitResult<MarkConversationAsReadError>
}