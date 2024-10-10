package com.thebrownfoxx.neon.client.repository.memory

import com.thebrownfoxx.neon.client.repository.group.GroupRepository
import com.thebrownfoxx.neon.client.repository.message.MessageRepository
import com.thebrownfoxx.neon.client.repository.message.model.AddMessageEntityError
import com.thebrownfoxx.neon.client.repository.message.model.GetConversationPreviewEntityError
import com.thebrownfoxx.neon.client.repository.message.model.GetConversationEntitiesError
import com.thebrownfoxx.neon.client.repository.message.model.GetMessageEntityError
import com.thebrownfoxx.neon.common.annotation.TestApi
import com.thebrownfoxx.neon.common.extension.coercedSubList
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

    @TestApi
    val messageList = messages.value.map { it.value }

    override fun get(id: MessageId): Flow<Result<Message, GetMessageEntityError>> {
        return messages.mapLatest { messages ->
            when (val message = messages[id]) {
                null -> Failure(GetMessageEntityError.NotFound)
                else -> Success(message)
            }
        }
    }

    override suspend fun add(message: Message): UnitResult<AddMessageEntityError> {
        val result = when {
            messages.value.containsKey(message.id) -> Failure(AddMessageEntityError.DuplicateId)
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
    ): Flow<Result<Set<GroupId>, GetConversationEntitiesError>> {
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
                        .coercedSubList(offset..<offset + count)
                        .map { (message) -> message.groupId }
                        .toSet()
                )
            }
        }
    }

    override fun getConversationPreview(
        id: GroupId,
    ): Flow<Result<MessageId, GetConversationPreviewEntityError>> {
        return messages.mapLatest { messages ->
            val message = messages.values
                .filter { it.groupId == id }
                .maxByOrNull { it.timestamp }

            when (message) {
                null -> Failure(GetConversationPreviewEntityError.NotFound)
                else -> Success(message.id)
            }
        }
    }

    override fun getMessages(
        groupId: GroupId,
        count: Int,
        offset: Int,
    ): Flow<Result<Set<MessageId>, GetMessageEntityError>> {
        return messages.mapLatest { messages ->
            val messageIds = messages.values
                .filter { it.groupId == groupId }
                .sortedByDescending { it.timestamp }
                .coercedSubList(offset..<offset + count)
                .map { it.id }
                .toSet()

            Success(messageIds)
        }
    }
}