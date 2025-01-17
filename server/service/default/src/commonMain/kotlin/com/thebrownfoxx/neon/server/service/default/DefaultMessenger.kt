package com.thebrownfoxx.neon.server.service.default

import com.thebrownfoxx.neon.common.data.AddError
import com.thebrownfoxx.neon.common.data.DataOperationError
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.data.transaction.transaction
import com.thebrownfoxx.neon.common.extension.flow.flow
import com.thebrownfoxx.neon.common.type.UnexpectedError
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.common.type.id.MessageId
import com.thebrownfoxx.neon.server.model.ChatGroup
import com.thebrownfoxx.neon.server.model.Delivery
import com.thebrownfoxx.neon.server.model.Message
import com.thebrownfoxx.neon.server.model.TimestampedMessageId
import com.thebrownfoxx.neon.server.repository.GroupMemberRepository
import com.thebrownfoxx.neon.server.repository.GroupRepository
import com.thebrownfoxx.neon.server.repository.MemberRepository
import com.thebrownfoxx.neon.server.repository.MessageRepository
import com.thebrownfoxx.neon.server.service.Messenger
import com.thebrownfoxx.neon.server.service.Messenger.GetConversationPreviewsError
import com.thebrownfoxx.neon.server.service.Messenger.GetMessageError
import com.thebrownfoxx.neon.server.service.Messenger.GetMessagesError
import com.thebrownfoxx.neon.server.service.Messenger.MarkConversationAsReadError
import com.thebrownfoxx.neon.server.service.Messenger.NewConversationError
import com.thebrownfoxx.neon.server.service.Messenger.SendMessageError
import com.thebrownfoxx.outcome.Failure
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.Success
import com.thebrownfoxx.outcome.UnitOutcome
import com.thebrownfoxx.outcome.UnitSuccess
import com.thebrownfoxx.outcome.map.getOrElse
import com.thebrownfoxx.outcome.map.mapError
import com.thebrownfoxx.outcome.map.onFailure
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.datetime.Clock

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultMessenger(
    private val messageRepository: MessageRepository,
    private val memberRepository: MemberRepository,
    private val groupRepository: GroupRepository,
    private val groupMemberRepository: GroupMemberRepository,
) : Messenger {
    override fun getConversationPreviews(
        actorId: MemberId,
    ): Flow<Outcome<List<Message>, GetConversationPreviewsError>> {
        return combine(
            memberRepository.getAsFlow(actorId),
            messageRepository.getConversationPreviewsAsFlow(actorId),
        ) { memberOutcome, conversationsOutcome ->
            memberOutcome
                .onFailure { return@combine Failure(it.toGetConversationPreviewsError()) }
            conversationsOutcome.mapError { GetConversationPreviewsError.UnexpectedError }
        }
    }

    override fun getMessages(
        actorId: MemberId,
        groupId: GroupId,
    ): Flow<Outcome<List<TimestampedMessageId>, GetMessagesError>> {
        return combine(
            groupRepository.getAsFlow(groupId),
            groupMemberRepository.getMembersAsFlow(groupId),
            messageRepository.getMessagesAsFlow(groupId),
        ) { groupOutcome, membersOutcome, messagesOutcome ->
            groupOutcome.getOrElse { return@combine Failure(it.getGroupErrorToGetMessagesError()) }

            val members = membersOutcome
                .getOrElse { return@combine Failure(it.toGetMessagesError()) }

            if (actorId !in members) return@combine Failure(GetMessagesError.Unauthorized)

            messagesOutcome.mapError { it.toGetMessagesError() }
        }
    }

    override fun getMessage(
        actorId: MemberId,
        id: MessageId,
    ): Flow<Outcome<Message, GetMessageError>> {
        return messageRepository.getAsFlow(id).flatMapLatest { messageOutcome ->
            val message = messageOutcome.getOrElse {
                return@flatMapLatest Failure(it.toGetMessageError()).flow()
            }

            groupMemberRepository.getMembersAsFlow(message.groupId)
                .mapLatest { groupMemberIdsOutcome ->
                    val groupMemberId = groupMemberIdsOutcome
                        .getOrElse { return@mapLatest Failure(GetMessageError.UnexpectedError) }

                    if (actorId !in groupMemberId)
                        return@mapLatest Failure(GetMessageError.Unauthorized)

                    Success(message)
                }
        }
    }

    override suspend fun newConversation(
        memberIds: Set<MemberId>,
    ): UnitOutcome<NewConversationError> {
        for (memberId in memberIds) {
            memberRepository.get(memberId).onFailure {
                return Failure(it.getMemberErrorToNewConversationError())
            }
        }

        val chatGroup = ChatGroup()

        return transaction {
            groupRepository.add(chatGroup).register()
                .onFailure { return@transaction Failure(NewConversationError.UnexpectedError) }

            for (memberId in memberIds) {
                groupMemberRepository.addMember(
                    groupId = chatGroup.id,
                    memberId = memberId,
                    isAdmin = false,
                ).register().onFailure {
                    return@transaction Failure(NewConversationError.UnexpectedError)
                }
            }

            UnitSuccess
        }
    }

    override suspend fun sendMessage(
        id: MessageId,
        actorId: MemberId,
        groupId: GroupId,
        content: String,
    ): UnitOutcome<SendMessageError> {
        groupRepository.get(groupId)
            .onFailure { return Failure(it.getGroupErrorToSendMessageError()) }

        val groupMemberIds = groupMemberRepository.getMembers(groupId)
            .getOrElse { return Failure(SendMessageError.UnexpectedError) }

        if (actorId !in groupMemberIds)
            return Failure(SendMessageError.Unauthorized)

        val message = Message(
            id = id,
            groupId = groupId,
            senderId = actorId,
            content = content,
            timestamp = Clock.System.now(),
            delivery = Delivery.Sent,
        )

        messageRepository.add(message).finalize()
            .onFailure { return Failure(it.toSendMessageError()) }

        return UnitSuccess
    }

    override suspend fun markConversationAsRead(
        actorId: MemberId,
        groupId: GroupId,
    ): UnitOutcome<MarkConversationAsReadError> {
        groupRepository.get(groupId)
            .onFailure { return Failure(it.getGroupErrorToMarkConversationAsReadError()) }

        val groupMemberIds = groupMemberRepository.getMembers(groupId)
            .getOrElse { return Failure(MarkConversationAsReadError.UnexpectedError) }

        if (actorId !in groupMemberIds)
            return Failure(MarkConversationAsReadError.Unauthorized)

        val unreadMessages = getUnreadMessages(groupId)
            .getOrElse { return Failure(MarkConversationAsReadError.UnexpectedError) }

        if (unreadMessages.isEmpty()) return Failure(MarkConversationAsReadError.AlreadyRead)

        return transaction {
            for (message in unreadMessages) {
                messageRepository.update(message.copy(delivery = Delivery.Read))
                    .register()
                    .onFailure {
                        return@transaction Failure(MarkConversationAsReadError.UnexpectedError)
                    }
            }

            UnitSuccess
        }
    }

    private suspend fun getUnreadMessages(
        groupId: GroupId,
    ): Outcome<List<Message>, UnexpectedError> {
        val unreadMessageIds =
            messageRepository.getUnreadMessages(groupId)
                .getOrElse { return Failure(UnexpectedError) }

        val unreadMessages = unreadMessageIds.map { id ->
            messageRepository.get(id).getOrElse { return Failure(UnexpectedError) }
        }

        return Success(unreadMessages)
    }

    private fun GetError.toGetConversationPreviewsError() = when (this) {
        GetError.NotFound -> GetConversationPreviewsError.MemberNotFound
        GetError.ConnectionError, GetError.UnexpectedError ->
            GetConversationPreviewsError.UnexpectedError
    }

    private fun GetError.getGroupErrorToGetMessagesError() = when (this) {
        GetError.NotFound -> GetMessagesError.GroupNotFound
        GetError.ConnectionError, GetError.UnexpectedError ->
            GetMessagesError.UnexpectedError
    }

    private fun DataOperationError.toGetMessagesError() = when (this) {
        DataOperationError.ConnectionError, DataOperationError.UnexpectedError ->
            GetMessagesError.UnexpectedError
    }

    private fun GetError.toGetMessageError() = when (this) {
        GetError.NotFound -> GetMessageError.NotFound
        GetError.ConnectionError, GetError.UnexpectedError -> GetMessageError.UnexpectedError
    }

    private fun GetError.getMemberErrorToNewConversationError() = when (this) {
        GetError.NotFound -> NewConversationError.MemberNotFound
        GetError.ConnectionError, GetError.UnexpectedError -> NewConversationError.UnexpectedError
    }

    private fun GetError.getGroupErrorToSendMessageError() = when (this) {
        GetError.NotFound -> SendMessageError.GroupNotFound
        GetError.ConnectionError, GetError.UnexpectedError -> SendMessageError.UnexpectedError
    }

    private fun AddError.toSendMessageError() = when (this) {
        AddError.Duplicate -> SendMessageError.DuplicateId
        AddError.ConnectionError, AddError.UnexpectedError -> SendMessageError.UnexpectedError
    }

    private fun GetError.getGroupErrorToMarkConversationAsReadError() =
        when (this) {
            GetError.NotFound -> MarkConversationAsReadError.GroupNotFound
            GetError.ConnectionError, GetError.UnexpectedError ->
                MarkConversationAsReadError.UnexpectedError
        }
}
