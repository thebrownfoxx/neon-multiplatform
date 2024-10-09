package com.thebrownfoxx.neon.client.repository.memory

import com.thebrownfoxx.neon.client.repository.group.GroupRepository
import com.thebrownfoxx.neon.client.repository.message.MessageRepository
import com.thebrownfoxx.neon.client.repository.message.model.AddMessageError
import com.thebrownfoxx.neon.client.repository.message.model.GetConversationPreviewError
import com.thebrownfoxx.neon.client.repository.message.model.GetConversationsError
import com.thebrownfoxx.neon.client.repository.message.model.GetMessageError
import com.thebrownfoxx.neon.common.model.Delivery
import com.thebrownfoxx.neon.common.model.Failure
import com.thebrownfoxx.neon.common.model.GroupId
import com.thebrownfoxx.neon.common.model.MemberId
import com.thebrownfoxx.neon.common.model.Message
import com.thebrownfoxx.neon.common.model.MessageId
import com.thebrownfoxx.neon.common.model.Result
import com.thebrownfoxx.neon.common.model.Success
import com.thebrownfoxx.neon.common.model.UnitResult
import com.thebrownfoxx.neon.common.model.getOrElse
import com.thebrownfoxx.neon.common.model.unitSuccess
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update

@OptIn(ExperimentalCoroutinesApi::class)
class InMemoryMessageRepository(private val groupRepository: GroupRepository) : MessageRepository {
    private val messages = MutableStateFlow<Map<MessageId, Message>>(emptyMap())

    override fun get(id: MessageId): Flow<Result<Message, GetMessageError>> {
        return messages.mapLatest { messages ->
            when (val message = messages[id]) {
                null -> Failure(GetMessageError.NotFound)
                else -> Success(message)
            }
        }
    }

    override suspend fun add(message: Message): UnitResult<AddMessageError> {
        val result = when {
            messages.value.containsKey(message.id) -> Failure(AddMessageError.DuplicateId)
            else -> {
                messages.update { it + (message.id to message) }
                unitSuccess()
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
    ): Flow<Result<Set<GroupId>, GetConversationsError>> {
        // TODO: OMG this is crazy

        return messages.flatMapLatest { messages ->
            val groupMemberIds = messages.values.map { message ->
                groupRepository.getMembers(message.groupId).map { membersResult ->
                    val members = membersResult.getOrElse { emptySet() }
                    message to members
                }
            }

            combine(groupMemberIds) {
                Success(
                    it
                        .filter { (message, groupMemberIds) ->
                            val sent = message.senderId == memberId
                            val messageRead = message.delivery == Delivery.Read || sent
                            messageRead == read && memberId in groupMemberIds
                        }
                        .sortedBy { (message) ->
                            message.timestamp.toEpochMilliseconds() * if (descending) -1 else 1
                        }
                        .subList(offset, offset + count)
                        .map { (message) -> message.groupId }
                        .toSet()
                )
            }
        }
    }

    override fun getConversationPreview(
        id: GroupId,
    ): Flow<Result<MessageId, GetConversationPreviewError>> {
        return messages.mapLatest { messages ->
            val message = messages.values
                .filter { it.groupId == id }
                .maxByOrNull { it.timestamp }

            when (message) {
                null -> Failure(GetConversationPreviewError.NotFound)
                else -> Success(message.id)
            }
        }
    }

    override fun getMessages(
        groupId: GroupId,
        count: Int,
        offset: Int,
    ): Flow<Result<Set<MessageId>, GetMessageError>> {
        return messages.mapLatest { messages ->
            val messageIds = messages.values
                .filter { it.groupId == groupId }
                .sortedByDescending { it.timestamp }
                .subList(offset, offset + count)
                .map { it.id }
                .toSet()

            Success(messageIds)
        }
    }
}