package com.thebrownfoxx.neon.server.repository.inmemory

import com.thebrownfoxx.neon.common.data.AddError
import com.thebrownfoxx.neon.common.data.DataOperationError
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.data.UpdateError
import com.thebrownfoxx.neon.common.data.transaction.ReversibleUnitOutcome
import com.thebrownfoxx.neon.common.data.transaction.asReversible
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.common.type.id.MessageId
import com.thebrownfoxx.neon.server.model.Delivery
import com.thebrownfoxx.neon.server.model.Message
import com.thebrownfoxx.neon.server.model.TimestampedMessageId
import com.thebrownfoxx.neon.server.repository.MessageRepository
import com.thebrownfoxx.outcome.Failure
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.Success
import com.thebrownfoxx.outcome.UnitSuccess
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update

@OptIn(ExperimentalCoroutinesApi::class)
class InMemoryMessageRepository : MessageRepository {
    private val messages = MutableStateFlow<Map<MessageId, Message>>(emptyMap())

    override fun getChatPreviewsAsFlow(memberId: MemberId): Flow<Outcome<List<Message>, DataOperationError>> {
        TODO("Not yet implemented")
    }

    override fun getMessagesAsFlow(
        groupId: GroupId,
    ): Flow<Outcome<List<TimestampedMessageId>, DataOperationError>> {
        return messages.mapLatest { messages ->
            val messageIds = messages.values
                .filter { it.groupId == groupId }
                .sortedByDescending { it.timestamp }
                .map { TimestampedMessageId(it.id, it.timestamp) }

            Success(messageIds)
        }
    }

    override fun getAsFlow(id: MessageId): Flow<Outcome<Message, GetError>> {
        return messages.mapLatest { messages ->
            when (val message = messages[id]) {
                null -> Failure(GetError.NotFound)
                else -> Success(message)
            }
        }
    }

    override suspend fun get(id: MessageId): Outcome<Message, GetError> {
        return getAsFlow(id).first()
    }

    override suspend fun add(message: Message): ReversibleUnitOutcome<AddError> {
        return when {
            messages.value.containsKey(message.id) -> Failure(AddError.Duplicate)
            else -> {
                messages.update { it + (message.id to message) }
                UnitSuccess
            }
        }.asReversible { messages.update { it - message.id } }
    }

    override suspend fun update(message: Message): ReversibleUnitOutcome<UpdateError> {
        if (!messages.value.containsKey(message.id))
            return Failure(UpdateError.NotFound).asReversible()
        messages.update { it + (message.id to message) }
        return UnitSuccess.asReversible { messages.update { it - message.id } }
    }

    override suspend fun getUnreadMessages(
        groupId: GroupId,
    ): Outcome<Set<MessageId>, DataOperationError> {
        return messages.mapLatest { messages ->
            val messageIds = messages.values
                .filter { it.groupId == groupId && it.delivery != Delivery.Read }
                .sortedByDescending { it.timestamp }
                .map { it.id }
                .toSet()

            Success(messageIds)
        }.first()
    }
}