package com.thebrownfoxx.neon.server.repository.inmemory

import com.thebrownfoxx.neon.common.data.AddError
import com.thebrownfoxx.neon.common.data.ConnectionError
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.data.UpdateError
import com.thebrownfoxx.neon.common.extension.coercedSubList
import com.thebrownfoxx.neon.common.type.Failure
import com.thebrownfoxx.neon.common.type.Outcome
import com.thebrownfoxx.neon.common.type.Success
import com.thebrownfoxx.neon.common.type.UnitOutcome
import com.thebrownfoxx.neon.common.type.getOrElse
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.common.type.id.MessageId
import com.thebrownfoxx.neon.common.type.unitSuccess
import com.thebrownfoxx.neon.server.model.Delivery
import com.thebrownfoxx.neon.server.model.Message
import com.thebrownfoxx.neon.server.repository.GroupMemberRepository
import com.thebrownfoxx.neon.server.repository.MessageRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update

@OptIn(ExperimentalCoroutinesApi::class)
class InMemoryMessageRepository(
    private val groupMemberRepository: GroupMemberRepository,
) : MessageRepository {
    private val messages = MutableStateFlow<Map<MessageId, Message>>(emptyMap())

    override fun get(id: MessageId): Flow<Outcome<Message, GetError>> {
        return messages.mapLatest { messages ->
            when (val message = messages[id]) {
                null -> Failure(GetError.NotFound)
                else -> Success(message)
            }
        }
    }

    override suspend fun add(message: Message): UnitOutcome<AddError> {
        return when {
            messages.value.containsKey(message.id) -> Failure(AddError.Duplicate)
            else -> {
                messages.update { it + (message.id to message) }
                unitSuccess()
            }
        }
    }

    override suspend fun update(message: Message): UnitOutcome<UpdateError> {
        if (!this.messages.value.containsKey(message.id)) return Failure(UpdateError.NotFound)
        this.messages.update { it + (message.id to message) }
        return unitSuccess()
    }

    override suspend fun getConversations(
        memberId: MemberId,
        count: Int,
        offset: Int,
        read: Boolean?,
        descending: Boolean,
    ): Outcome<Set<GroupId>, ConnectionError> {
        // TODO: OMG this is crazy

        return messages.flatMapLatest { messages ->
            val groupMemberIds = messages.values.map { message ->
                groupMemberRepository.getMembers(message.groupId).map { membersOutcome ->
                    val members = membersOutcome.getOrElse { emptyList() }
                    message to members
                }
            }

            combine(groupMemberIds) {
                Success(
                    it
                        .filter { (message, groupMemberIds) ->
                            val sent = message.senderId == memberId
                            val messageRead = message.delivery == Delivery.Read || sent
                            (read == null || messageRead == read) && memberId in groupMemberIds
                        }
                        .sortedBy { (message) ->
                            message.timestamp.toEpochMilliseconds() * if (descending) -1 else 1
                        }
                        .coercedSubList(offset..<offset + count)
                        .map { (message) -> message.groupId }
                        .toSet()
                )
            }
        }.first()
    }

    override fun getConversationCount(
        memberId: MemberId,
        read: Boolean?,
    ): Flow<Outcome<Int, ConnectionError>> {
        return messages.flatMapLatest { messages ->
            val groupMemberIds = messages.values.map { message ->
                groupMemberRepository.getMembers(message.groupId).map { membersOutcome ->
                    val members = membersOutcome.getOrElse { emptyList() }
                    message to members
                }
            }

            combine(groupMemberIds) {
                Success(
                    it.count { (message, groupMemberIds) ->
                        val sent = message.senderId == memberId
                        val messageRead = message.delivery == Delivery.Read || sent
                        (read == null || messageRead == read) && memberId in groupMemberIds
                    }
                )
            }
        }
    }

    override fun getConversationPreview(
        id: GroupId,
    ): Flow<Outcome<MessageId?, ConnectionError>> {
        return messages.mapLatest { messages ->
            val message = messages.values
                .filter { it.groupId == id }
                .maxByOrNull { it.timestamp }

            Success(message?.id)
        }
    }

    override fun getMessages(
        groupId: GroupId,
        count: Int,
        offset: Int,
    ): Flow<Outcome<Set<MessageId>, ConnectionError>> {
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

    override fun getUnreadMessages(
        groupId: GroupId,
    ): Flow<Outcome<Set<MessageId>, ConnectionError>> {
        return messages.mapLatest { messages ->
            val messageIds = messages.values
                .filter { it.groupId == groupId && it.delivery != Delivery.Read }
                .sortedByDescending { it.timestamp }
                .map { it.id }
                .toSet()

            Success(messageIds)
        }
    }
}