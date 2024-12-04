package com.thebrownfoxx.neon.client.repository.local

import com.thebrownfoxx.neon.client.model.LocalConversations
import com.thebrownfoxx.neon.client.model.LocalMessage
import com.thebrownfoxx.neon.common.data.ConnectionError
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.outcome.Outcome
import com.thebrownfoxx.neon.common.outcome.UnitOutcome
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MessageId
import kotlinx.coroutines.flow.Flow

interface LocalMessageDataSource {
    // TODO: Maybe reflect changes to server Messsenger? Just have conversationPreviews
    //  and deprecate conversations and getConversationPreviewAsFlow
    val conversations: Flow<Outcome<LocalConversations, ConnectionError>>
    fun getConversationPreviewAsFlow(groupId: GroupId): Flow<Outcome<MessageId?, ConnectionError>>
    fun getMessageAsFlow(id: MessageId): Flow<Outcome<LocalMessage, GetError>>
    suspend fun upsert(message: LocalMessage): UnitOutcome<ConnectionError>
    suspend fun batchUpsert(messages: List<LocalMessage>): UnitOutcome<ConnectionError>
}