package com.thebrownfoxx.neon.server.service.default

import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.data.transaction.transaction
import com.thebrownfoxx.neon.common.extension.flow
import com.thebrownfoxx.neon.common.type.UnexpectedError
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
import com.thebrownfoxx.outcome.getOrElse
import com.thebrownfoxx.outcome.memberBlockContext
import com.thebrownfoxx.outcome.onFailure
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
        memberBlockContext("getConversationPreviews") {
            return combine(
                memberRepository.getAsFlow(actorId),
                messageRepository.getConversationPreviewsAsFlow(actorId),
            ) { memberOutcome, conversationsOutcome ->
                memberOutcome
                    .onFailure { return@combine mapError(error.toGetConversationPreviewsError()) }
                conversationsOutcome.mapError { GetConversationPreviewsError.UnexpectedError }
            }
        }
    }

    override fun getMessage(
        actorId: MemberId,
        id: MessageId,
    ): Flow<Outcome<Message, GetMessageError>> {
        memberBlockContext("getMessage") {
            return messageRepository.getAsFlow(id).flatMapLatest { messageOutcome ->
                val message = messageOutcome.getOrElse {
                    return@flatMapLatest mapError(error.toGetMessageError()).flow()
                }

                groupMemberRepository.getMembersAsFlow(message.groupId)
                    .mapLatest { groupMemberIdsOutcome ->
                        val groupMemberId = groupMemberIdsOutcome
                            .getOrElse { return@mapLatest mapError(GetMessageError.UnexpectedError) }

                        if (actorId !in groupMemberId)
                            return@mapLatest Failure(GetMessageError.Unauthorized)

                        Success(message)
                    }
            }
        }
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
        memberBlockContext("newConversation") {
            for (memberId in memberIds) {
                memberRepository.get(memberId).onFailure {
                    return mapError(error.getMemberErrorToNewConversationError(memberId))
                }
            }

            val chatGroup = ChatGroup()

            return transaction {
                groupRepository.add(chatGroup).register()
                    .onFailure { return@transaction mapError(NewConversationError.UnexpectedError) }

                for (memberId in memberIds) {
                    groupMemberRepository.addMember(
                        groupId = chatGroup.id,
                        memberId = memberId,
                        isAdmin = false,
                    ).register().onFailure {
                        return@transaction mapError(NewConversationError.UnexpectedError)
                    }
                }

                UnitSuccess
            }
        }
    }

    override suspend fun sendMessage(
        actorId: MemberId,
        groupId: GroupId,
        content: String,
    ): UnitOutcome<SendMessageError> {
        memberBlockContext("sendMessage") {
            groupRepository.get(groupId)
                .onFailure { return mapError(error.getGroupErrorToSendMessageError(groupId)) }

            val groupMemberIds = groupMemberRepository.getMembers(groupId)
                .getOrElse { return mapError(SendMessageError.UnexpectedError) }

            if (actorId !in groupMemberIds) return Failure(SendMessageError.Unauthorized(actorId))

            val message = Message(
                groupId = groupId,
                senderId = actorId,
                content = content,
                timestamp = Clock.System.now(),
                delivery = Delivery.Sent,
            )

            messageRepository.add(message).result
                .onFailure { return mapError(SendMessageError.UnexpectedError) }

            return UnitSuccess
        }
    }

    override suspend fun markConversationAsRead(
        actorId: MemberId,
        groupId: GroupId,
    ): UnitOutcome<MarkConversationAsReadError> {
        memberBlockContext("markConversationAsRead") {
            groupRepository.get(groupId)
                .onFailure { return mapError(getGroupErrorToMarkConversationAsReadError(groupId)) }

            val groupMemberIds = groupMemberRepository.getMembers(groupId)
                .getOrElse { return mapError(MarkConversationAsReadError.UnexpectedError) }

            if (actorId !in groupMemberIds)
                return Failure(MarkConversationAsReadError.Unauthorized(actorId))

            val unreadMessages = getUnreadMessages(groupId)
                .getOrElse { return mapError(MarkConversationAsReadError.UnexpectedError) }

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
    }

    private suspend fun getUnreadMessages(
        groupId: GroupId,
    ): Outcome<List<Message>, UnexpectedError> {
        memberBlockContext("getUnreadMessages") {
            val unreadMessageIds =
                messageRepository.getUnreadMessages(groupId)
                    .getOrElse { return mapError(UnexpectedError) }

            val unreadMessages = unreadMessageIds.map { id ->
                messageRepository.get(id).getOrElse { return mapError(UnexpectedError) }
            }

            return Success(unreadMessages)
        }
    }

    private fun GetError.toGetConversationPreviewsError() = when (this) {
        GetError.NotFound -> GetConversationPreviewsError.MemberNotFound
        GetError.ConnectionError, GetError.UnexpectedError ->
            GetConversationPreviewsError.UnexpectedError
    }

    private fun GetError.toGetMessageError() = when (this) {
        GetError.NotFound -> GetMessageError.NotFound
        GetError.ConnectionError, GetError.UnexpectedError -> GetMessageError.UnexpectedError
    }

    private fun GetError.getMemberErrorToNewConversationError(memberId: MemberId) = when (this) {
        GetError.NotFound -> NewConversationError.MemberNotFound(memberId)
        GetError.ConnectionError, GetError.UnexpectedError -> NewConversationError.UnexpectedError
    }

    private fun GetError.getGroupErrorToSendMessageError(groupId: GroupId) = when (this) {
        GetError.NotFound -> SendMessageError.GroupNotFound(groupId)
        GetError.ConnectionError, GetError.UnexpectedError -> SendMessageError.UnexpectedError
    }

    private fun Failure<GetError>.getGroupErrorToMarkConversationAsReadError(groupId: GroupId) =
        when (error) {
            GetError.NotFound -> MarkConversationAsReadError.GroupNotFound(groupId)
            GetError.ConnectionError, GetError.UnexpectedError ->
                MarkConversationAsReadError.UnexpectedError
        }
}
