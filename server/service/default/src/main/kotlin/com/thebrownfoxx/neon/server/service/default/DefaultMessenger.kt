package com.thebrownfoxx.neon.server.service.default

import com.thebrownfoxx.neon.common.extension.asFlow
import com.thebrownfoxx.neon.common.type.Failure
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.common.type.id.MessageId
import com.thebrownfoxx.neon.common.type.Outcome
import com.thebrownfoxx.neon.common.type.Success
import com.thebrownfoxx.neon.common.type.UnitOutcome
import com.thebrownfoxx.neon.common.type.asFailure
import com.thebrownfoxx.neon.common.type.asSuccess
import com.thebrownfoxx.neon.common.type.getOrElse
import com.thebrownfoxx.neon.common.type.mapError
import com.thebrownfoxx.neon.common.type.onFailure
import com.thebrownfoxx.neon.common.type.unitSuccess
import com.thebrownfoxx.neon.server.model.ChatGroup
import com.thebrownfoxx.neon.server.model.Delivery
import com.thebrownfoxx.neon.server.model.Message
import com.thebrownfoxx.neon.server.repository.group.GroupRepository
import com.thebrownfoxx.neon.server.repository.group.model.RepositoryAddGroupError
import com.thebrownfoxx.neon.server.repository.group.model.RepositoryGetGroupError
import com.thebrownfoxx.neon.server.repository.groupmember.GroupMemberRepository
import com.thebrownfoxx.neon.server.repository.groupmember.model.RepositoryAddGroupMemberError
import com.thebrownfoxx.neon.server.repository.groupmember.model.RepositoryGetGroupMembersError
import com.thebrownfoxx.neon.server.repository.member.MemberRepository
import com.thebrownfoxx.neon.server.repository.member.model.RepositoryGetMemberError
import com.thebrownfoxx.neon.server.repository.message.MessageRepository
import com.thebrownfoxx.neon.server.repository.message.model.RepositoryAddMessageError
import com.thebrownfoxx.neon.server.repository.message.model.RepositoryGetConversationCountError
import com.thebrownfoxx.neon.server.repository.message.model.RepositoryGetConversationPreviewError
import com.thebrownfoxx.neon.server.repository.message.model.RepositoryGetConversationsError
import com.thebrownfoxx.neon.server.repository.message.model.RepositoryGetMessageError
import com.thebrownfoxx.neon.server.repository.message.model.RepositoryGetMessagesError
import com.thebrownfoxx.neon.server.repository.message.model.RepositoryUpdateMessageError
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
import kotlinx.coroutines.flow.first
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

    override suspend fun getConversations(
        actorId: MemberId,
        count: Int,
        offset: Int,
    ): Outcome<Conversations, GetConversationsError> {
        memberRepository.get(actorId).first().onFailure { error ->
            return when (error) {
                RepositoryGetMemberError.NotFound ->
                    GetConversationsError.MemberNotFound(actorId)

                RepositoryGetMemberError.ConnectionError ->
                    GetConversationsError.ConnectionError
            }.asFailure()
        }

        val nudgedConversations = getNudgedConversations(
            actorId = actorId,
            count = count,
            offset = offset,
        ).getOrElse { return Failure(it.toGetConversationsError()) }

        val unreadConversations = getUnreadConversations(
            actorId = actorId,
            count = count - nudgedConversations.size,
            offset = offset - maxNudgedCount,
        ).getOrElse { return Failure(it.toGetConversationsError()) }

        val unreadCount = messageRepository.getConversationCount(memberId = actorId, read = false)
            .first()
            .getOrElse { error ->
                return when (error) {
                    RepositoryGetConversationCountError.ConnectionError ->
                        GetConversationsError.ConnectionError
                }.asFailure()
            }

        val readConversations = getReadConversations(
            actorId = actorId,
            count = count - nudgedConversations.size - unreadConversations.size,
            offset = offset - maxNudgedCount - unreadCount,
        ).getOrElse { return Failure(it.toGetConversationsError()) }

        return Conversations(
            nudgedGroupIds = nudgedConversations,
            unreadGroupIds = unreadConversations,
            readGroupIds = readConversations,
        ).asSuccess()
    }

    private suspend fun getNudgedConversations(
        actorId: MemberId,
        count: Int,
        offset: Int,
    ): Outcome<Set<GroupId>, RepositoryGetConversationsError> {
        val nudgedCount = min(maxNudgedCount - offset, count)
        return messageRepository.getConversations(
            memberId = actorId,
            count = nudgedCount,
            offset = offset,
            read = false,
            descending = true,
        ).first()
    }

    private suspend fun getUnreadConversations(
        actorId: MemberId,
        count: Int,
        offset: Int,
    ): Outcome<Set<GroupId>, RepositoryGetConversationsError> {
        return messageRepository.getConversations(
            memberId = actorId,
            count = count,
            offset = offset,
            read = false,
        ).first()
    }

    private suspend fun getReadConversations(
        actorId: MemberId,
        count: Int,
        offset: Int,
    ): Outcome<Set<GroupId>, RepositoryGetConversationsError> {
        return messageRepository.getConversations(
            memberId = actorId,
            count = count,
            offset = offset,
            read = true,
        ).first()
    }

    private fun RepositoryGetConversationsError.toGetConversationsError() = when (this) {
        RepositoryGetConversationsError.ConnectionError -> GetConversationsError.ConnectionError
    }

    override fun getMessage(
        actorId: MemberId,
        id: MessageId,
    ): Flow<Outcome<Message, GetMessageError>> {
        return messageRepository.get(id).flatMapLatest { messageOutcome ->
            val message = messageOutcome.getOrElse { error ->
                return@flatMapLatest when (error) {
                    RepositoryGetMessageError.NotFound -> GetMessageError.NotFound(id)
                    RepositoryGetMessageError.ConnectionError -> GetMessageError.ConnectionError
                }.asFailure().asFlow()
            }

            groupMemberRepository.getMembers(message.groupId).mapLatest { groupMemberIdsOutcome ->
                val groupMemberId = groupMemberIdsOutcome.getOrElse { error ->
                    return@mapLatest when (error) {
                        RepositoryGetGroupMembersError.ConnectionError ->
                            GetMessageError.ConnectionError
                    }.asFailure()
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
        return groupRepository.get(groupId).flatMapLatest { group ->
            group.onFailure { error ->
                return@flatMapLatest when (error) {
                    RepositoryGetGroupError.NotFound ->
                        GetConversationPreviewError.GroupNotFound(groupId)

                    RepositoryGetGroupError.ConnectionError ->
                        GetConversationPreviewError.ConnectionError
                }.asFailure().asFlow()
            }
            getConversationPreviewFromRepository(groupId, actorId)
        }
    }

    private fun getConversationPreviewFromRepository(
        groupId: GroupId,
        actorId: MemberId,
    ): Flow<Outcome<MessageId?, GetConversationPreviewError>> {
        return messageRepository.getConversationPreview(groupId).flatMapLatest { messageOutcome ->
            val previewId = messageOutcome.getOrElse { error ->
                return@flatMapLatest when (error) {
                    RepositoryGetConversationPreviewError.ConnectionError ->
                        GetConversationPreviewError.ConnectionError
                }.asFailure().asFlow()
            }

            groupMemberRepository.getMembers(groupId).mapLatest { groupMemberIdsOutcome ->
                val groupMemberId = groupMemberIdsOutcome.getOrElse { error ->
                    return@mapLatest when (error) {
                        RepositoryGetGroupMembersError.ConnectionError ->
                            GetConversationPreviewError.ConnectionError
                    }.asFailure()
                }

                if (actorId !in groupMemberId)
                    return@mapLatest Failure(GetConversationPreviewError.Unauthorized(actorId))

                Success(previewId)
            }
        }
    }

    override suspend fun getMessages(
        actorId: MemberId,
        groupId: GroupId,
        count: Int,
        offset: Int,
    ): Outcome<Set<MessageId>, GetMessagesError> {
        groupRepository.get(groupId).first().onFailure { error ->
            return when (error) {
                RepositoryGetGroupError.NotFound -> GetMessagesError.GroupNotFound(groupId)
                RepositoryGetGroupError.ConnectionError -> GetMessagesError.ConnectionError
            }.asFailure()
        }

        val groupMemberIds = groupMemberRepository.getMembers(groupId).first().getOrElse { error ->
            return when (error) {
                RepositoryGetGroupMembersError.ConnectionError -> GetMessagesError.ConnectionError
            }.asFailure()
        }

        if (actorId !in groupMemberIds) return Failure(GetMessagesError.Unauthorized(actorId))

        return messageRepository.getMessages(groupId, count, offset).first().mapError { error ->
            when (error) {
                RepositoryGetMessagesError.ConnectionError -> GetMessagesError.ConnectionError
            }
        }
    }

    override suspend fun newConversation(
        memberIds: Set<MemberId>,
    ): UnitOutcome<NewConversationError> {
        for (memberId in memberIds) {
            memberRepository.get(memberId).first().onFailure { error ->
                return when (error) {
                    RepositoryGetMemberError.NotFound ->
                        NewConversationError.MemberNotFound(memberId)

                    RepositoryGetMemberError.ConnectionError -> NewConversationError.ConnectionError
                }.asFailure()
            }
        }

        val chatGroup = ChatGroup()
        groupRepository.add(chatGroup).onFailure { error ->
            return when (error) {
                RepositoryAddGroupError.DuplicateId -> error("What are the chances?")
                RepositoryAddGroupError.ConnectionError -> NewConversationError.ConnectionError
            }.asFailure()
        }

        for (memberId in memberIds) {
            groupMemberRepository.addMember(
                groupId = chatGroup.id,
                memberId = memberId,
                admin = false,
            ).onFailure { error ->
                return when (error) {
                    RepositoryAddGroupMemberError.ConnectionError ->
                        NewConversationError.ConnectionError

                    RepositoryAddGroupMemberError.DuplicateMembership -> error("Can't be?")
                }.asFailure()
            }
        }

        return unitSuccess()
    }

    override suspend fun sendMessage(
        actorId: MemberId,
        groupId: GroupId,
        content: String,
    ): UnitOutcome<SendMessageError> {
        groupRepository.get(groupId).first().onFailure { error ->
            return when (error) {
                RepositoryGetGroupError.NotFound -> SendMessageError.GroupNotFound(groupId)
                RepositoryGetGroupError.ConnectionError -> SendMessageError.ConnectionError
            }.asFailure()
        }

        val groupMemberIds = groupMemberRepository.getMembers(groupId).first().getOrElse { error ->
            return when (error) {
                RepositoryGetGroupMembersError.ConnectionError -> SendMessageError.ConnectionError
            }.asFailure()
        }

        if (actorId !in groupMemberIds) return Failure(SendMessageError.Unauthorized(actorId))

        val message = Message(
            groupId = groupId,
            senderId = actorId,
            content = content,
            timestamp = Clock.System.now(),
            delivery = Delivery.Sent,
        )

        messageRepository.add(message).onFailure { error ->
            return when (error) {
                RepositoryAddMessageError.DuplicateId -> error("What are the chances?")
                RepositoryAddMessageError.ConnectionError -> SendMessageError.ConnectionError
            }.asFailure()
        }

        return unitSuccess()
    }

    override suspend fun markConversationAsRead(
        actorId: MemberId,
        groupId: GroupId,
    ): UnitOutcome<MarkConversationAsReadError> {
        groupRepository.get(groupId).first().onFailure { error ->
            return when (error) {
                RepositoryGetGroupError.NotFound ->
                    MarkConversationAsReadError.GroupNotFound(groupId)

                RepositoryGetGroupError.ConnectionError ->
                    MarkConversationAsReadError.ConnectionError
            }.asFailure()
        }

        val groupMemberIds = groupMemberRepository.getMembers(groupId).first().getOrElse { error ->
            return when (error) {
                RepositoryGetGroupMembersError.ConnectionError ->
                    MarkConversationAsReadError.ConnectionError
            }.asFailure()
        }

        if (actorId !in groupMemberIds)
            return Failure(MarkConversationAsReadError.Unauthorized(actorId))

        val unreadMessageIds =
            messageRepository.getUnreadMessages(groupId).first().getOrElse { error ->
                return when (error) {
                    RepositoryGetMessagesError.ConnectionError ->
                        MarkConversationAsReadError.ConnectionError
                }.asFailure()
            }

        val unreadMessages = unreadMessageIds.map {
            messageRepository.get(it).first().getOrElse { error ->
                when (error) {
                    RepositoryGetMessageError.NotFound -> null
                    RepositoryGetMessageError.ConnectionError ->
                        return Failure(MarkConversationAsReadError.ConnectionError)
                }
            }
        }.filterNotNull()

        if (unreadMessages.isEmpty()) return Failure(MarkConversationAsReadError.AlreadyRead)

        for (message in unreadMessages) {
            messageRepository.update(message.copy(delivery = Delivery.Read)).onFailure { error ->
                when (error) {
                    RepositoryUpdateMessageError.NotFound -> {}
                    RepositoryUpdateMessageError.ConnectionError ->
                        return Failure(MarkConversationAsReadError.ConnectionError)
                }
            }
        }

        return unitSuccess()
    }
}