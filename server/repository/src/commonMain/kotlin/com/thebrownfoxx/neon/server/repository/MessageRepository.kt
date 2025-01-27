package com.thebrownfoxx.neon.server.repository

import com.thebrownfoxx.neon.common.data.AddError
import com.thebrownfoxx.neon.common.data.DataOperationError
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.data.UpdateError
import com.thebrownfoxx.neon.common.data.transaction.ReversibleUnitOutcome
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.common.type.id.MessageId
import com.thebrownfoxx.neon.server.model.Message
import com.thebrownfoxx.neon.server.model.TimestampedMessageId
import com.thebrownfoxx.outcome.Outcome
import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    fun getChatPreviewsAsFlow(
        memberId: MemberId,
    ): Flow<Outcome<List<Message>, DataOperationError>>

    fun getMessagesAsFlow(
        groupId: GroupId,
    ): Flow<Outcome<List<TimestampedMessageId>, DataOperationError>>

    fun getAsFlow(id: MessageId): Flow<Outcome<Message, GetError>>

    suspend fun get(id: MessageId): Outcome<Message, GetError>

    suspend fun add(message: Message): ReversibleUnitOutcome<AddError>

    suspend fun update(message: Message): ReversibleUnitOutcome<UpdateError>

    @Deprecated("Use DeliveryRepository instead")
    suspend fun getUnreadMessages(
        memberId: MemberId,
        groupId: GroupId,
    ): Outcome<List<Message>, DataOperationError>
}