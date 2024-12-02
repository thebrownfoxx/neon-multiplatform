package com.thebrownfoxx.neon.server.service.default

import com.thebrownfoxx.neon.common.data.AddError
import com.thebrownfoxx.neon.common.data.ConnectionError
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.data.UpdateError
import com.thebrownfoxx.neon.common.data.transaction.transaction
import com.thebrownfoxx.neon.common.extension.flow
import com.thebrownfoxx.neon.common.outcome.Failure
import com.thebrownfoxx.neon.common.outcome.Outcome
import com.thebrownfoxx.neon.common.outcome.Success
import com.thebrownfoxx.neon.common.outcome.UnitOutcome
import com.thebrownfoxx.neon.common.outcome.asFailure
import com.thebrownfoxx.neon.common.outcome.asSuccess
import com.thebrownfoxx.neon.common.outcome.getOrElse
import com.thebrownfoxx.neon.common.outcome.mapError
import com.thebrownfoxx.neon.common.outcome.onFailure
import com.thebrownfoxx.neon.common.outcome.unitSuccess
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.common.type.id.MessageId
import com.thebrownfoxx.neon.server.model.ChatGroup
import com.thebrownfoxx.neon.server.model.Delivery
import com.thebrownfoxx.neon.server.model.Message
import com.thebrownfoxx.neon.server.repository.GroupMemberRepository
import com.thebrownfoxx.neon.server.repository.GroupRepository
import com.thebrownfoxx.neon.server.repository.MemberRepository
import com.thebrownfoxx.neon.server.repository.MessageRepository
import com.thebrownfoxx.neon.server.service.messenger.Messenger
import com.thebrownfoxx.neon.server.service.messenger.model.Conversations
import com.thebrownfoxx.neon.server.service.messenger.model.GetConversationPreviewError
import com.thebrownfoxx.neon.server.service.messenger.model.GetConversationsError
import com.thebrownfoxx.neon.server.service.messenger.model.GetMessageError
import com.thebrownfoxx.neon.server.service.messenger.model.GetMessagesError
import com.thebrownfoxx.neon.server.service.messenger.model.MarkConversationAsReadError
import com.thebrownfoxx.neon.server.service.messenger.model.NewConversationError
import com.thebrownfoxx.neon.server.service.messenger.model.SendMessageError
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.datetime.Clock
import kotlin.math.min

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultMessenger(
    private val messageRepository: MessageRepository,
    private val memberRepository: MemberRepository,
    private val groupRepository: GroupRepository,
    private val groupMemberRepository: GroupMemberRepository,
) : Messenger {
    private val maxNudgedCount = 2

    @Suppress("DEPRECATION")
    @Deprecated("Use getConversations(MemberId) instead")
    override suspend fun getConversations(
        actorId: MemberId,
        count: Int,
        offset: Int,
    ): Outcome<Conversations, GetConversationsError> {
        memberRepository.get(actorId).onFailure { error ->
            return when (error) {
                GetError.NotFound -> GetConversationsError.MemberNotFound(actorId)
                GetError.ConnectionError -> GetConversationsError.ConnectionError
            }.asFailure()
        }

        val nudgedConversations = getNudgedConversations(
            actorId = actorId,
            count = count,
            offset = offset,
        ).getOrElse { return Failure(GetConversationsError.ConnectionError) }

        val unreadConversations = getUnreadConversations(
            actorId = actorId,
            count = count - nudgedConversations.size,
            offset = offset - maxNudgedCount,
        ).getOrElse { return Failure(GetConversationsError.ConnectionError) }

        val unreadCount = messageRepository.getConversationCount(memberId = actorId, read = false)
            .getOrElse { return Failure(GetConversationsError.ConnectionError) }

        val readConversations = getReadConversations(
            actorId = actorId,
            count = count - nudgedConversations.size - unreadConversations.size,
            offset = offset - maxNudgedCount - unreadCount,
        ).getOrElse { return Failure(GetConversationsError.ConnectionError) }

        return Conversations(
            nudgedGroupIds = nudgedConversations,
            unreadGroupIds = unreadConversations,
            readGroupIds = readConversations,
        ).asSuccess()
    }

    @Suppress("DEPRECATION")
    @Deprecated("Use getConversations(MemberId) instead")
    private suspend fun getNudgedConversations(
        actorId: MemberId,
        count: Int,
        offset: Int,
    ): Outcome<Set<GroupId>, ConnectionError> {
        val nudgedCount = min(maxNudgedCount - offset, count)
        return messageRepository.getConversations(
            memberId = actorId,
            count = nudgedCount,
            offset = offset,
            read = false,
            descending = true,
        )
    }

    @Suppress("DEPRECATION")
    @Deprecated("Use getConversations(MemberId) instead")
    private suspend fun getUnreadConversations(
        actorId: MemberId,
        count: Int,
        offset: Int,
    ): Outcome<Set<GroupId>, ConnectionError> {
        return messageRepository.getConversations(
            memberId = actorId,
            count = count,
            offset = offset,
            read = false,
        )
    }

    @Suppress("DEPRECATION")
    @Deprecated("Use getConversations(MemberId) instead")
    private suspend fun getReadConversations(
        actorId: MemberId,
        count: Int,
        offset: Int,
    ): Outcome<Set<GroupId>, ConnectionError> {
        return messageRepository.getConversations(
            memberId = actorId,
            count = count,
            offset = offset,
            read = true,
        )
    }

    override suspend fun getConversations(
        actorId: MemberId,
    ): Outcome<Conversations, GetConversationsError> {
        memberRepository.get(actorId).onFailure { error ->
            return when (error) {
                GetError.NotFound -> GetConversationsError.MemberNotFound(actorId)
                GetError.ConnectionError -> GetConversationsError.ConnectionError
            }.asFailure()
        }

        val conversations = messageRepository.getConversations(actorId).getOrElse {
            return Failure(GetConversationsError.ConnectionError)
        }

        val nudgedConversations = when {
            conversations.unreadGroupIds.size > 10 ->
                conversations.unreadGroupIds.take(maxNudgedCount)
            else -> emptyList()
        }.toSet()

        return Success(
            Conversations(
                nudgedGroupIds = nudgedConversations,
                unreadGroupIds = conversations.unreadGroupIds,
                readGroupIds = conversations.readGroupIds,
            )
        )
    }

    override fun getMessage(
        actorId: MemberId,
        id: MessageId,
    ): Flow<Outcome<Message, GetMessageError>> {
        return messageRepository.getAsFlow(id).flatMapLatest { messageOutcome ->
            val message = messageOutcome.getOrElse { error ->
                return@flatMapLatest when (error) {
                    GetError.NotFound -> GetMessageError.NotFound(id)
                    GetError.ConnectionError -> GetMessageError.ConnectionError
                }.asFailure().flow()
            }

            groupMemberRepository.getMembersAsFlow(message.groupId)
                .mapLatest { groupMemberIdsOutcome ->
                    val groupMemberId = groupMemberIdsOutcome.getOrElse {
                        return@mapLatest Failure(GetMessageError.ConnectionError)
                    }

                    if (actorId !in groupMemberId)
                        return@mapLatest Failure(GetMessageError.Unauthorized(actorId))

                    Success(message)
                }
        }
    }

    override fun getConversationPreview(
        actorId: MemberId,
        groupId: GroupId,
    ): Flow<Outcome<MessageId?, GetConversationPreviewError>> {
        return groupRepository.getAsFlow(groupId).flatMapLatest { group ->
            group.onFailure { error ->
                return@flatMapLatest when (error) {
                    GetError.NotFound -> GetConversationPreviewError.GroupNotFound(groupId)
                    GetError.ConnectionError -> GetConversationPreviewError.ConnectionError
                }.asFailure().flow()
            }
            getConversationPreviewFromRepository(groupId, actorId)
        }
    }

    private fun getConversationPreviewFromRepository(
        groupId: GroupId,
        actorId: MemberId,
    ): Flow<Outcome<MessageId?, GetConversationPreviewError>> {
        return messageRepository.getConversationPreviewAsFlow(groupId).flatMapLatest { messageOutcome ->
            val previewId = messageOutcome.getOrElse {
                return@flatMapLatest Failure(GetConversationPreviewError.ConnectionError).flow()
            }

            groupMemberRepository.getMembersAsFlow(groupId).mapLatest { groupMemberIdsOutcome ->
                val groupMemberId = groupMemberIdsOutcome.getOrElse {
                    return@mapLatest Failure(GetConversationPreviewError.ConnectionError)
                }

                if (actorId !in groupMemberId)
                    return@mapLatest Failure(GetConversationPreviewError.Unauthorized(actorId))

                Success(previewId)
            }
        }
    }

    @Deprecated("Use getMessages(MemberId, GroupId) instead")
    override suspend fun getMessages(
        actorId: MemberId,
        groupId: GroupId,
        count: Int,
        offset: Int,
    ): Outcome<Set<MessageId>, GetMessagesError> {
        groupRepository.get(groupId).onFailure { error ->
            return when (error) {
                GetError.NotFound -> GetMessagesError.GroupNotFound(groupId)
                GetError.ConnectionError -> GetMessagesError.ConnectionError
            }.asFailure()
        }

        val groupMemberIds = groupMemberRepository.getMembers(groupId).getOrElse {
            return Failure(GetMessagesError.ConnectionError)
        }

        if (actorId !in groupMemberIds) return Failure(GetMessagesError.Unauthorized(actorId))

        return messageRepository.getMessages(groupId, count, offset)
            .mapError { GetMessagesError.ConnectionError }
    }

    override suspend fun getMessages(
        actorId: MemberId,
        groupId: GroupId,
    ): Outcome<Set<MessageId>, GetMessagesError> {
        TODO("Not yet implemented")
    }

    override suspend fun newConversation(
        memberIds: Set<MemberId>,
    ): UnitOutcome<NewConversationError> {
        for (memberId in memberIds) {
            memberRepository.get(memberId).onFailure { error ->
                return when (error) {
                    GetError.NotFound -> NewConversationError.MemberNotFound(memberId)
                    GetError.ConnectionError -> NewConversationError.ConnectionError
                }.asFailure()
            }
        }

        val chatGroup = ChatGroup()

        return transaction {
            groupRepository.add(chatGroup).register().onFailure { error ->
                return@transaction when (error) {
                    AddError.Duplicate -> error("What are the chances?")
                    AddError.ConnectionError -> NewConversationError.ConnectionError
                }.asFailure()
            }

            for (memberId in memberIds) {
                groupMemberRepository.addMember(
                    groupId = chatGroup.id,
                    memberId = memberId,
                    isAdmin = false,
                ).register().onFailure { error ->
                    return@transaction when (error) {
                        AddError.ConnectionError -> NewConversationError.ConnectionError
                        AddError.Duplicate -> error("Can't be?")
                    }.asFailure()
                }
            }

            unitSuccess()
        }
    }

    override suspend fun sendMessage(
        actorId: MemberId,
        groupId: GroupId,
        content: String,
    ): UnitOutcome<SendMessageError> {
        groupRepository.get(groupId).onFailure { error ->
            return when (error) {
                GetError.NotFound -> SendMessageError.GroupNotFound(groupId)
                GetError.ConnectionError -> SendMessageError.ConnectionError
            }.asFailure()
        }

        val groupMemberIds = groupMemberRepository.getMembers(groupId)
            .getOrElse { return Failure(SendMessageError.ConnectionError) }

        if (actorId !in groupMemberIds) return Failure(SendMessageError.Unauthorized(actorId))

        val message = Message(
            groupId = groupId,
            senderId = actorId,
            content = content,
            timestamp = Clock.System.now(),
            delivery = Delivery.Sent,
        )

        messageRepository.add(message).result.onFailure { error ->
            return when (error) {
                AddError.Duplicate -> error("What are the chances?")
                AddError.ConnectionError -> SendMessageError.ConnectionError
            }.asFailure()
        }

        return unitSuccess()
    }

    override suspend fun markConversationAsRead(
        actorId: MemberId,
        groupId: GroupId,
    ): UnitOutcome<MarkConversationAsReadError> {
        groupRepository.get(groupId).onFailure { error ->
            return when (error) {
                GetError.NotFound -> MarkConversationAsReadError.GroupNotFound(groupId)
                GetError.ConnectionError -> MarkConversationAsReadError.ConnectionError
            }.asFailure()
        }

        val groupMemberIds = groupMemberRepository.getMembers(groupId)
            .getOrElse { return Failure(MarkConversationAsReadError.ConnectionError) }

        if (actorId !in groupMemberIds)
            return Failure(MarkConversationAsReadError.Unauthorized(actorId))

        val unreadMessageIds =
            messageRepository.getUnreadMessages(groupId)
                .getOrElse { return Failure(MarkConversationAsReadError.ConnectionError) }

        val unreadMessages = unreadMessageIds.map {
            messageRepository.get(it).getOrElse { error ->
                when (error) {
                    GetError.NotFound -> null
                    GetError.ConnectionError ->
                        return Failure(MarkConversationAsReadError.ConnectionError)
                }
            }
        }.filterNotNull()

        if (unreadMessages.isEmpty()) return Failure(MarkConversationAsReadError.AlreadyRead)

        return transaction {
            for (message in unreadMessages) {
                messageRepository.update(message.copy(delivery = Delivery.Read)).register()
                    .onFailure { error ->
                        when (error) {
                            UpdateError.NotFound -> {}
                            UpdateError.ConnectionError ->
                                return@transaction Failure(MarkConversationAsReadError.ConnectionError)
                        }
                    }
            }

            unitSuccess()
        }
    }
}