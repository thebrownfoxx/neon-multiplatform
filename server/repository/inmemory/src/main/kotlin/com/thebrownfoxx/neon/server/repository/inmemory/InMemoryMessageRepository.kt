package com.thebrownfoxx.neon.server.repository.inmemory

import com.thebrownfoxx.neon.common.data.AddError
import com.thebrownfoxx.neon.common.data.ConnectionError
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.data.UpdateError
import com.thebrownfoxx.neon.common.data.transaction.ReversibleUnitOutcome
import com.thebrownfoxx.neon.common.data.transaction.asReversible
import com.thebrownfoxx.neon.common.extension.coercedSubList
import com.thebrownfoxx.neon.common.outcome.Failure
import com.thebrownfoxx.neon.common.outcome.Outcome
import com.thebrownfoxx.neon.common.outcome.Success
import com.thebrownfoxx.neon.common.outcome.unitSuccess
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.common.type.id.MessageId
import com.thebrownfoxx.neon.server.model.Delivery
import com.thebrownfoxx.neon.server.model.Message
import com.thebrownfoxx.neon.server.repository.GroupMemberRepository
import com.thebrownfoxx.neon.server.repository.MessageRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update

@OptIn(ExperimentalCoroutinesApi::class)
class InMemoryMessageRepository(
    private val groupMemberRepository: GroupMemberRepository,
) : MessageRepository {
    private val messages = MutableStateFlow<Map<MessageId, Message>>(emptyMap())

    override fun getAsFlow(id: MessageId): Flow<Outcome<Message, GetError>> {
        return messages.mapLatest { messages ->
            when (val message = messages[id]) {
                null -> Failure(GetError.NotFound)
                else -> Success(message)
            }
        }
    }

    override fun getConversationPreviewAsFlow(
        id: GroupId,
    ): Flow<Outcome<MessageId?, ConnectionError>> {
        return messages.mapLatest { messages ->
            val message = messages.values
                .filter { it.groupId == id }
                .maxByOrNull { it.timestamp }

            Success(message?.id)
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
                unitSuccess()
            }
        }.asReversible { messages.update { it - message.id } }
    }

    override suspend fun update(message: Message): ReversibleUnitOutcome<UpdateError> {
        if (!this.messages.value.containsKey(message.id))
            return Failure(UpdateError.NotFound).asReversible()
        this.messages.update { it + (message.id to message) }
        return unitSuccess().asReversible { this.messages.update { it - message.id } }
    }

    override suspend fun getConversationsAsFlow(memberId: MemberId): Flow<Outcome<Set<GroupId>, ConnectionError>> {
        TODO("Not yet implemented")
    }

    override suspend fun getMessages(
        groupId: GroupId,
        count: Int,
        offset: Int,
    ): Outcome<Set<MessageId>, ConnectionError> {
        return messages.mapLatest { messages ->
            val messageIds = messages.values
                .filter { it.groupId == groupId }
                .sortedByDescending { it.timestamp }
                .coercedSubList(offset..<offset + count)
                .map { it.id }
                .toSet()

            Success(messageIds)
        }.first()
    }

    override suspend fun getUnreadMessages(
        groupId: GroupId,
    ): Outcome<Set<MessageId>, ConnectionError> {
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