package com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation

import com.thebrownfoxx.neon.client.application.aggregator.GroupAggregator
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.ConversationInfoState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.ConversationPaneState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.ConversationState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.GroupPosition
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.MessageEntry
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.MessageListEntry
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.MessageSenderState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.MessageState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.ReceivedDirectMessageState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.ReceivedGroupMessageState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.SentMessageState
import com.thebrownfoxx.neon.client.application.ui.state.getAvatarState
import com.thebrownfoxx.neon.client.application.ui.state.toAvatarState
import com.thebrownfoxx.neon.client.application.ui.state.toChatGroupName
import com.thebrownfoxx.neon.client.application.ui.state.toDeliveryState
import com.thebrownfoxx.neon.client.model.LocalChatGroup
import com.thebrownfoxx.neon.client.model.LocalCommunity
import com.thebrownfoxx.neon.client.model.LocalGroup
import com.thebrownfoxx.neon.client.model.LocalMessage
import com.thebrownfoxx.neon.client.model.LocalTimestampedMessageId
import com.thebrownfoxx.neon.client.service.Authenticator
import com.thebrownfoxx.neon.client.service.GroupManager
import com.thebrownfoxx.neon.client.service.MemberManager
import com.thebrownfoxx.neon.client.service.Messenger
import com.thebrownfoxx.neon.common.Logger
import com.thebrownfoxx.neon.common.extension.flow.combineOrEmpty
import com.thebrownfoxx.neon.common.extension.flow.flow
import com.thebrownfoxx.neon.common.extension.flow.mirror
import com.thebrownfoxx.neon.common.extension.toLocalDateTime
import com.thebrownfoxx.neon.common.type.Loadable
import com.thebrownfoxx.neon.common.type.Loaded
import com.thebrownfoxx.neon.common.type.Loading
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.outcome.ThrowingApi
import com.thebrownfoxx.outcome.map.getOrThrow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class, ThrowingApi::class)
class ConversationStateHandler(
    authenticator: Authenticator,
    private val groupManager: GroupManager,
    private val memberManager: MemberManager,
    private val messenger: Messenger,
    selectedConversationGroupId: Flow<GroupId?>,
    message: Flow<String>,
    externalScope: CoroutineScope,
    private val logger: Logger,
) {
    // TODO: Lazy load the messages, just like in ChatPreviewsStateHandler

    private val groupAggregator = GroupAggregator(groupManager, memberManager)

    private val conversation = authenticator.loggedInMemberId.flatMapLatest { loggedInMemberId ->
        selectedConversationGroupId.flatMapLatest groupId@{ groupId ->
            if (groupId == null) return@groupId flowOf(null)
            getConversation(loggedInMemberId, groupId)
        }
    }

    val paneState = combine(
        conversation,
        message,
    ) { conversation, message ->
        ConversationPaneState(
            conversation = conversation ?: return@combine null,
            message = message,
        )
    }
        .catch { logger.logError(it) }
        .stateIn(externalScope, SharingStarted.Eagerly, null)

    private fun getConversation(
        loggedInMemberId: MemberId?,
        groupId: GroupId,
    ): Flow<ConversationState> {
        return groupManager.getGroup(groupId).flatMapLatest { groupOutcome ->
            groupOutcome.getOrThrow().toConversationState(loggedInMemberId)
        }
    }

    private fun LocalGroup.toConversationState(
        loggedInMemberId: MemberId?,
    ): Flow<ConversationState> {
        val infoFlow = getLoadableInfo(loggedInMemberId)
        val entriesFlow = getMessageListEntries(loggedInMemberId)
        return combine(
            infoFlow,
            entriesFlow,
        ) { info, entries ->
            ConversationState(
                groupId = id,
                info = info,
                entries = entries,
                loading = false,
            )
        }
    }

    private fun LocalGroup.getLoadableInfo(
        loggedInMemberId: MemberId?,
    ): Flow<Loadable<ConversationInfoState>> {
        return flow {
            emit(Loading) // TODO: This will show a loading indicator whenever this function is called :(
            mirror(getInfo(loggedInMemberId)) { Loaded(it) }
        }
    }

    private fun LocalGroup.getInfo(loggedInMemberId: MemberId?): Flow<ConversationInfoState> {
        return when (this) {
            is LocalChatGroup -> getInfo(loggedInMemberId)
            is LocalCommunity -> getInfo(loggedInMemberId)
        }
    }

    private fun LocalChatGroup.getInfo(
        loggedInMemberId: MemberId?,
    ): Flow<ConversationInfoState> {
        return groupAggregator.getMembers(
            groupId = id,
            excludedIds = listOfNotNull(loggedInMemberId),
        ).mapLatest {
            ConversationInfoState(
                avatar = it.toAvatarState(),
                name = it.toChatGroupName(),
            )
        }
    }

    private fun LocalCommunity.getInfo(
        loggedInMemberId: MemberId?,
    ): Flow<ConversationInfoState> {
        return groupAggregator.getCommunityAvatar(
            loggedInMemberId = loggedInMemberId,
            community = this,
        ).mapLatest {
            ConversationInfoState(avatar = it, name = name)
        }
    }

    private fun LocalGroup.getMessageListEntries(
        loggedInMemberId: MemberId?,
    ): Flow<List<MessageListEntry>> {
        return messenger.getMessages(id).flatMapLatest { messagesOutcome ->
            messagesOutcome.getOrThrow().map { timestampedMessageId ->
                getLocalMessageWithSenderState(timestampedMessageId, loggedInMemberId)
            }.combineOrEmpty { it.toMessageListEntries() }
        }
    }

    private fun LocalGroup.getLocalMessageWithSenderState(
        timestampedMessageId: LocalTimestampedMessageId,
        loggedInMemberId: MemberId?,
    ): Flow<LocalMessageWithSenderState> {
        val directFlow = when (this) {
            is LocalChatGroup -> groupAggregator.getMembers(
                groupId = id,
                excludedIds = listOfNotNull(loggedInMemberId),
            ).mapLatest { it.size > 1 }
            else -> flowOf(false)
        }
        return messenger.getMessage(timestampedMessageId.id).flatMapLatest { messageOutcome ->
            directFlow.flatMapLatest { direct ->
                messageOutcome.getOrThrow().toLocalMessageWithSenderState(loggedInMemberId, direct)
            }
        }
    }

    private fun LocalMessage.toLocalMessageWithSenderState(
        loggedInMemberId: MemberId?,
        direct: Boolean,
    ): Flow<LocalMessageWithSenderState> {
        val sender = when {
            loggedInMemberId == senderId -> SentMessageState.flow()
            direct -> ReceivedDirectMessageState.flow()
            else -> memberManager.getMember(senderId).mapLatest { memberOutcome ->
                val member = memberOutcome.getOrThrow()
                ReceivedGroupMessageState(member.getAvatarState())
            }
        }
        return sender.mapLatest {
            LocalMessageWithSenderState(message = this, sender = it)
        }
    }

    private fun Array<LocalMessageWithSenderState>.toMessageListEntries(): List<MessageEntry> {
        return mapIndexed { index, message ->
            val previous = getOrNull(index - 1)
            val next = getOrNull(index + 1)
            message.toMessageEntry(previous, next)
        }
    }

    private fun LocalMessageWithSenderState.toMessageEntry(
        previous: LocalMessageWithSenderState?,
        next: LocalMessageWithSenderState?,
    ): MessageEntry {
        val previousDifferent = previous == null || previous.sender::class != sender::class
        val nextDifferent = next == null || next.sender::class != sender::class

        val groupPosition = getGroupPosition(previousDifferent, nextDifferent)

        return with(message) {
            MessageState(
                id = id,
                content = content,
                timestamp = timestamp.toLocalDateTime(),
                delivery = delivery.toDeliveryState(),
                groupPosition = groupPosition,
                sender = sender,
            ).toMessageEntry(mustSpace = previousDifferent)
        }
    }

    private fun getGroupPosition(
        previousDifferent: Boolean,
        nextDifferent: Boolean,
    ): GroupPosition {
        return when {
            previousDifferent && nextDifferent -> GroupPosition.Alone
            previousDifferent -> GroupPosition.First
            nextDifferent -> GroupPosition.Last
            else -> GroupPosition.Middle
        }
    }

    private fun MessageState.toMessageEntry(mustSpace: Boolean = false) = MessageEntry(
        message = this,
        mustSpace = mustSpace,
    )

    private data class LocalMessageWithSenderState(
        val message: LocalMessage,
        val sender: MessageSenderState,
    )
}