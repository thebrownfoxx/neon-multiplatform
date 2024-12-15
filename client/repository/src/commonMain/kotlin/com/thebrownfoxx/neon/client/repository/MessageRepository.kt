package com.thebrownfoxx.neon.client.repository

import com.thebrownfoxx.neon.client.model.LocalConversationPreviews
import com.thebrownfoxx.neon.client.model.LocalMessage
import com.thebrownfoxx.neon.common.data.DataOperationError
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MessageId
import com.thebrownfoxx.outcome.Outcome
import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    val conversationPreviewsFlow: Flow<Outcome<LocalConversationPreviews, DataOperationError>>
    fun getMessagesAsFlow(groupId: GroupId): Flow<Outcome<Set<MessageId>, DataOperationError>>
    fun getAsFlow(id: MessageId): Flow<Outcome<LocalMessage, GetError>>
}