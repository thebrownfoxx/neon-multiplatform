package com.thebrownfoxx.neon.client.repository.memory

import com.thebrownfoxx.neon.client.repository.MessageRepository
import com.thebrownfoxx.neon.client.repository.model.AddEntityError
import com.thebrownfoxx.neon.client.repository.model.AddEntityResult
import com.thebrownfoxx.neon.client.repository.model.GetEntitiesResult
import com.thebrownfoxx.neon.client.repository.model.GetEntityError
import com.thebrownfoxx.neon.client.repository.model.GetEntityResult
import com.thebrownfoxx.neon.common.model.Delivery
import com.thebrownfoxx.neon.common.model.Failure
import com.thebrownfoxx.neon.common.model.GroupId
import com.thebrownfoxx.neon.common.model.MemberId
import com.thebrownfoxx.neon.common.model.Message
import com.thebrownfoxx.neon.common.model.MessageId
import com.thebrownfoxx.neon.common.model.Success
import com.thebrownfoxx.neon.common.model.UnitSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class InMemoryMessageRepository : MessageRepository {
    private val messages = mutableMapOf<MessageId, Message>()

    override fun get(id: MessageId): Flow<GetEntityResult<Message>> {
        val result = when (val message = messages[id]) {
            null -> Failure(GetEntityError.NotFound)
            else -> Success(message)
        }

        return flowOf(result)
    }

    override suspend fun add(message: Message): AddEntityResult {
        val result = when {
            messages.containsKey(message.id) -> Failure(AddEntityError.DuplicateId)
            else -> {
                messages[message.id] = message
                UnitSuccess()
            }
        }

        return result
    }

    override fun getConversations(
        memberId: MemberId,
        count: Int,
        offset: Int,
        read: Boolean,
        descending: Boolean,
    ): Flow<GetEntitiesResult<GroupId>> {
        val messages = messages.values.filter {
            val sent = it.senderId == memberId
            val messageRead = it.delivery == Delivery.Read || sent
            messageRead == read
        }
            .sortedBy { it.timestamp.toEpochMilliseconds() * if (descending) -1 else 1 }
            .subList(offset, offset + count)
            .map { it.groupId }
            .distinct()

        return flowOf(Success(messages))
    }

    override fun getConversationPreview(id: GroupId): Flow<GetEntityResult<MessageId?>> {
        val message = messages.values
            .filter { it.groupId == id }
            .maxByOrNull { it.timestamp }

        val result = when (message) {
            null -> Failure(GetEntityError.NotFound)
            else -> Success(message.id)
        }

        return flowOf(result)
    }

    override fun getMessages(
        groupId: GroupId,
        count: Int,
        offset: Int,
    ): Flow<GetEntitiesResult<MessageId>> {
        val messages = messages.values
            .filter { it.groupId == groupId }
            .sortedByDescending { it.timestamp }
            .subList(offset, offset + count)
            .map { it.id }

        return flowOf(Success(messages))
    }
}