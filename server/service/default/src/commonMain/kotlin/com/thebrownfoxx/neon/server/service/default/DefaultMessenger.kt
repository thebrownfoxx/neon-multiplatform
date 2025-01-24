package com.thebrownfoxx.neon.server.service.default

import com.thebrownfoxx.neon.common.data.AddError
import com.thebrownfoxx.neon.common.data.DataOperationError
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.data.transaction.ReversibleUnitOutcome
import com.thebrownfoxx.neon.common.data.transaction.flatMap
import com.thebrownfoxx.neon.common.data.transaction.transaction
import com.thebrownfoxx.neon.common.extension.flow.flow
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.common.type.id.MessageId
import com.thebrownfoxx.neon.server.model.ChatGroup
import com.thebrownfoxx.neon.server.model.Delivery
import com.thebrownfoxx.neon.server.model.Message
import com.thebrownfoxx.neon.server.model.TimestampedMessageId
import com.thebrownfoxx.neon.server.repository.DeliveryRepository
import com.thebrownfoxx.neon.server.repository.GroupMemberRepository
import com.thebrownfoxx.neon.server.repository.GroupRepository
import com.thebrownfoxx.neon.server.repository.MemberRepository
import com.thebrownfoxx.neon.server.repository.MessageRepository
import com.thebrownfoxx.neon.server.service.Messenger
import com.thebrownfoxx.neon.server.service.Messenger.GetChatPreviewsError
import com.thebrownfoxx.neon.server.service.Messenger.GetDeliveryError
import com.thebrownfoxx.neon.server.service.Messenger.GetMessageError
import com.thebrownfoxx.neon.server.service.Messenger.GetMessagesError
import com.thebrownfoxx.neon.server.service.Messenger.MarkAsReadError
import com.thebrownfoxx.neon.server.service.Messenger.NewConversationError
import com.thebrownfoxx.neon.server.service.Messenger.SendMessageError
import com.thebrownfoxx.neon.server.service.Messenger.UpdateDeliveryError
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
    private val deliveryRepository: DeliveryRepository,
    private val memberRepository: MemberRepository,
    private val groupRepository: GroupRepository,
    private val groupMemberRepository: GroupMemberRepository,
) : Messenger {
    override fun getChatPreviews(
        actorId: MemberId,
    ): Flow<Outcome<List<Message>, GetChatPreviewsError>> {
        return combine(
            memberRepository.getAsFlow(actorId),
            messageRepository.getChatPreviewsAsFlow(actorId),
        ) { memberOutcome, conversationsOutcome ->
            memberOutcome
                .onFailure { return@combine Failure(it.toGetChatPreviewsError()) }
            conversationsOutcome.mapError { GetChatPreviewsError.UnexpectedError }
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
            val message = messageOutcome.getOrElse { error ->
                return@flatMapLatest Failure(error.toGetMessageError()).flow()
            }
            val groupMembers = groupMemberRepository.getMembersAsFlow(message.groupId)
            groupMembers.mapLatest { groupMemberIdsOutcome ->
                val groupMemberId = groupMemberIdsOutcome
                    .getOrElse { return@mapLatest Failure(GetMessageError.UnexpectedError) }

                if (actorId !in groupMemberId)
                    return@mapLatest Failure(GetMessageError.Unauthorized)

                Success(message)
            }
        }
    }

    override fun getDelivery(
        actorId: MemberId,
        messageId: MessageId,
    ): Flow<Outcome<Delivery, GetDeliveryError>> {
        return messageRepository.getAsFlow(messageId).flatMapLatest { messageOutcome ->
            val message = messageOutcome.getOrElse { error ->
                return@flatMapLatest Failure(error.getMessageErrorToGetDeliveryError()).flow()
            }
            val groupMembers = groupMemberRepository.getMembersAsFlow(message.groupId)
            groupMembers.flatMapLatest groupMembers@{ groupMemberIdsOutcome ->
                val groupMemberId = groupMemberIdsOutcome.getOrElse {
                    return@groupMembers Failure(GetDeliveryError.UnexpectedError).flow()
                }

                if (actorId !in groupMemberId)
                    return@groupMembers Failure(GetDeliveryError.Unauthorized).flow()

                deliveryRepository.getAsFlow(messageId, actorId).mapLatest { deliveryOutcome ->
                    deliveryOutcome.mapError { GetDeliveryError.UnexpectedError }
                }
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

        val unreadMessages = messageRepository.getUnreadMessages(actorId, groupId)
            .getOrElse { return Failure(SendMessageError.UnexpectedError) }

        return transaction {
            unreadMessages.markAsRead(actorId).register().onFailure {
                return@transaction Failure(SendMessageError.UnexpectedError)
            }

            messageRepository.add(message).register()
                .onFailure { return@transaction Failure(it.toSendMessageError()) }

            UnitSuccess
        }
    }

    @Deprecated("Use update delivery instead")
    override suspend fun markAsRead(
        actorId: MemberId,
        groupId: GroupId,
    ): UnitOutcome<MarkAsReadError> {
        groupRepository.get(groupId)
            .onFailure { return Failure(it.getGroupErrorToMarkAsReadError()) }

        val groupMemberIds = groupMemberRepository.getMembers(groupId)
            .getOrElse { return Failure(MarkAsReadError.UnexpectedError) }

        if (actorId !in groupMemberIds)
            return Failure(MarkAsReadError.Unauthorized)

        val unreadMessages = messageRepository.getUnreadMessages(actorId, groupId)
            .getOrElse { return Failure(MarkAsReadError.UnexpectedError) }

        if (unreadMessages.isEmpty()) return Failure(MarkAsReadError.AlreadyRead)

        return transaction {
            unreadMessages.markAsRead(actorId).register().onFailure {
                return@transaction Failure(MarkAsReadError.UnexpectedError)
            }
            UnitSuccess
        }
    }

    override suspend fun updateDelivery(
        actorId: MemberId,
        messageId: MessageId,
        delivery: Delivery,
    ): UnitOutcome<UpdateDeliveryError> {     
        val message = messageRepository.get(messageId)
            .getOrElse { return Failure(it.getMessageErrorToUpdateDeliveryError()) }

        val groupMemberIds = groupMemberRepository.getMembers(message.groupId)
            .getOrElse { return Failure(UpdateDeliveryError.UnexpectedError) }

        if (actorId !in groupMemberIds)
            return Failure(UpdateDeliveryError.Unauthorized)

        val oldDelivery = deliveryRepository.get(messageId, actorId)
            .getOrElse { return Failure(UpdateDeliveryError.UnexpectedError) }

        if (delivery.ordinal < oldDelivery.ordinal)
            return Failure(UpdateDeliveryError.ReverseDelivery(oldDelivery))

        if (delivery == oldDelivery)
            return Failure(UpdateDeliveryError.DeliveryAlreadySet)

        return deliveryRepository.set(messageId, actorId, delivery).finalize()
            .mapError { UpdateDeliveryError.UnexpectedError }
    }

    private suspend fun List<Message>.markAsRead(
        actorId: MemberId,
    ): ReversibleUnitOutcome<DataOperationError> {
        return map { message ->
            deliveryRepository.set(message.id, actorId, Delivery.Read)
        }.flatMap {}
    }

    private fun GetError.toGetChatPreviewsError() = when (this) {
        GetError.NotFound -> GetChatPreviewsError.MemberNotFound
        GetError.ConnectionError, GetError.UnexpectedError ->
            GetChatPreviewsError.UnexpectedError
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

    private fun GetError.getMessageErrorToGetDeliveryError() = when (this) {
        GetError.NotFound -> GetDeliveryError.MessageNotFound
        GetError.ConnectionError, GetError.UnexpectedError -> GetDeliveryError.UnexpectedError
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

    private fun GetError.getGroupErrorToMarkAsReadError() = when (this) {
        GetError.NotFound -> MarkAsReadError.GroupNotFound
        GetError.ConnectionError, GetError.UnexpectedError ->
            MarkAsReadError.UnexpectedError
    }

    private fun GetError.getMessageErrorToUpdateDeliveryError() = when (this) {
        GetError.NotFound -> UpdateDeliveryError.MessageNotFound
        GetError.ConnectionError, GetError.UnexpectedError ->
            UpdateDeliveryError.UnexpectedError
    }
}
