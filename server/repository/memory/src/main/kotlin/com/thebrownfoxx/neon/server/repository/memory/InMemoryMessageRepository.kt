package com.thebrownfoxx.neon.server.repository.memory

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
import com.thebrownfoxx.neon.server.repository.groupmember.GroupMemberRepository
import com.thebrownfoxx.neon.server.repository.message.MessageRepository
import com.thebrownfoxx.neon.server.repository.message.model.RepositoryAddMessageError
import com.thebrownfoxx.neon.server.repository.message.model.RepositoryGetConversationPreviewError
import com.thebrownfoxx.neon.server.repository.message.model.RepositoryGetConversationsError
import com.thebrownfoxx.neon.server.repository.message.model.RepositoryGetMessageError
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update

@OptIn(ExperimentalCoroutinesApi::class)
class InMemoryMessageRepository(
    private val groupMemberRepository: GroupMemberRepository,
) : MessageRepository {
    private val messages = MutableStateFlow<Map<MessageId, Message>>(emptyMap())

    @TestApi
    val messageList = messages.value.map { it.value }

    override fun get(id: MessageId): Flow<Result<Message, RepositoryGetMessageError>> {
        return messages.mapLatest { messages ->
            when (val message = messages[id]) {
                null -> Failure(RepositoryGetMessageError.NotFound)
                else -> Success(message)
            }
        }
    }

    override suspend fun add(message: Message): UnitResult<RepositoryAddMessageError> {
        val result = when {
            messages.value.containsKey(message.id) -> Failure(RepositoryAddMessageError.DuplicateId)
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
    ): Flow<Result<Set<GroupId>, RepositoryGetConversationsError>> {
        // TODO: OMG this is crazy

        return messages.flatMapLatest { messages ->
            val groupMemberIds = messages.values.map { message ->
                groupMemberRepository.getMembers(message.groupId).map { membersResult ->
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
    ): Flow<Result<MessageId, RepositoryGetConversationPreviewError>> {
        return messages.mapLatest { messages ->
            val message = messages.values
                .filter { it.groupId == id }
                .maxByOrNull { it.timestamp }

            when (message) {
                null -> Failure(RepositoryGetConversationPreviewError.NotFound)
                else -> Success(message.id)
            }
        }
    }

    override fun getMessages(
        groupId: GroupId,
        count: Int,
        offset: Int,
    ): Flow<Result<Set<MessageId>, RepositoryGetMessageError>> {
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