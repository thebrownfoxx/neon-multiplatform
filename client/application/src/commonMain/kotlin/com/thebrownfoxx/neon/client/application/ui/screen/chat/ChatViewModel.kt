package com.thebrownfoxx.neon.client.application.ui.screen.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thebrownfoxx.neon.client.application.ui.component.avatar.state.AvatarState
import com.thebrownfoxx.neon.client.application.ui.component.avatar.state.GroupAvatarState
import com.thebrownfoxx.neon.client.application.ui.component.avatar.state.SingleAvatarState
import com.thebrownfoxx.neon.client.application.ui.component.delivery.state.DeliveryState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.ConversationPaneState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.ConversationState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.GroupPosition
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.MessageEntry
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.MessageState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.ReceivedCommunityMessageState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.ReceivedDirectMessageState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.SentMessageState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ChatPreviewContentState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ChatPreviewState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ChatPreviewsState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.LoadedChatPreviewState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.LoadingChatPreviewState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ReceivedCommunityChatPreviewState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ReceivedDirectChatPreviewState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.SentChatPreviewState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.state.ConversationDummy
import com.thebrownfoxx.neon.client.model.LocalChatGroup
import com.thebrownfoxx.neon.client.model.LocalCommunity
import com.thebrownfoxx.neon.client.model.LocalConversationPreviews
import com.thebrownfoxx.neon.client.model.LocalDelivery
import com.thebrownfoxx.neon.client.model.LocalMember
import com.thebrownfoxx.neon.client.model.LocalMessage
import com.thebrownfoxx.neon.client.service.Authenticator
import com.thebrownfoxx.neon.client.service.GroupManager
import com.thebrownfoxx.neon.client.service.MemberManager
import com.thebrownfoxx.neon.client.service.Messenger
import com.thebrownfoxx.neon.common.Logger
import com.thebrownfoxx.neon.common.extension.combineOrEmpty
import com.thebrownfoxx.neon.common.extension.flow
import com.thebrownfoxx.neon.common.extension.toLocalDateTime
import com.thebrownfoxx.neon.common.type.Loading
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.outcome.map.getOrThrow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModel(
    private val authenticator: Authenticator,
    private val groupManager: GroupManager,
    private val memberManager: MemberManager,
    private val messenger: Messenger,
    private val logger: Logger,
) : ViewModel() {
    // TODO: This shouldn't be like this. It should start with a more representative loader
    private val loadingState = ChatPreviewsState(
        nudgedConversations = emptyList(),
        unreadConversations = emptyList(),
        readConversations = List(20) { LoadingChatPreviewState() },
    )

    private val lastVisiblePreview = MutableStateFlow<GroupId?>(null)

    val chatPreviews = messenger.conversationPreviews.flatMapLatest { conversationPreviewsOutcome ->
        lastVisiblePreview.flatMapLatest { lastVisiblePreview ->
            val conversationPreviews = conversationPreviewsOutcome.getOrThrow()

            val lastVisiblePreviewIndex =
                conversationPreviews.getLastVisiblePreviewIndex(lastVisiblePreview)
            val lastIndexToLoad = lastVisiblePreviewIndex + 20

            val nudgedPreviews = conversationPreviews.nudgedPreviews.mapIndexed { index, item ->
                item.toChatPreviewState(index <= lastIndexToLoad)
            }

            val unreadPreviews = conversationPreviews.unreadPreviews.mapIndexed { index, item ->
                val overallIndex = index + nudgedPreviews.size
                item.toChatPreviewState(overallIndex <= lastIndexToLoad)
            }

            val readPreviews = conversationPreviews.readPreviews.mapIndexed { index, item ->
                val overallIndex = index + nudgedPreviews.size + unreadPreviews.size
                item.toChatPreviewState(overallIndex <= lastIndexToLoad)
            }

            combine(
                combineOrEmpty(nudgedPreviews) { it },
                combineOrEmpty(unreadPreviews) { it },
                combineOrEmpty(readPreviews) { it },
            ) { nudged, unread, read ->
                ChatPreviewsState(
                    nudgedConversations = nudged.toList(),
                    unreadConversations = unread.toList(),
                    readConversations = read.toList(),
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, loadingState)

    fun onConversationClick(chatPreviewState: ChatPreviewState) {
        val dummy = ConversationDummy.ConversationPaneState
        conversationGroup.value = chatPreviewState.groupId
    }

    fun onLastVisiblePreviewChange(groupId: GroupId) {
        lastVisiblePreview.value = groupId
    }

    fun onConversationClose() {
        conversationGroup.value = null
    }

    private fun LocalConversationPreviews.getLastVisiblePreviewIndex(
        lastVisiblePreview: GroupId?,
    ): Int {
        return when {
            nudgedPreviews.any { it.groupId == lastVisiblePreview } ->
                nudgedPreviews.indexOfFirst { it.groupId == lastVisiblePreview }

            unreadPreviews.any { it.groupId == lastVisiblePreview } -> nudgedPreviews.size +
                    unreadPreviews.indexOfFirst { it.groupId == lastVisiblePreview }

            readPreviews.any { it.groupId == lastVisiblePreview } -> nudgedPreviews.size +
                    unreadPreviews.size +
                    readPreviews.indexOfFirst { it.groupId == lastVisiblePreview }

            else -> 0
        }
    }

    private fun LocalMessage.toChatPreviewState(
        mustBeLoaded: Boolean,
    ): Flow<ChatPreviewState> {
        return when {
            mustBeLoaded -> toChatPreviewState()
            else -> LoadingChatPreviewState(groupId).flow()
        }
    }

    private fun LocalMessage.toChatPreviewState(): Flow<ChatPreviewState> {
        return authenticator.loggedInMember.flatMapLatest { loggedInMemberId ->
            groupManager.getGroup(groupId).flatMapLatest { groupOutcome ->
                when (val group = groupOutcome.getOrThrow()) {
                    is LocalChatGroup -> toLoadedChatPreviewState(loggedInMemberId, group)
                    is LocalCommunity -> toLoadedChatPreviewState(loggedInMemberId, group)
                }
            }.stateIn(viewModelScope, SharingStarted.Eagerly, LoadingChatPreviewState(groupId))
        }
    }

    private fun LocalMessage.toLoadedChatPreviewState(
        loggedInMemberId: MemberId?,
        chatGroup: LocalChatGroup,
    ): Flow<LoadedChatPreviewState> {
        val senderState = when (senderId) {
            loggedInMemberId -> SentChatPreviewState
            else -> ReceivedDirectChatPreviewState
        }

        return chatGroup.getMemberToShow(loggedInMemberId).mapLatest { member ->
            LoadedChatPreviewState(
                groupId = groupId,
                avatar = member.getAvatarState(),
                name = member.username,
                content = ChatPreviewContentState(
                    message = content,
                    timestamp = timestamp.toLocalDateTime(),
                    delivery = delivery.toDeliveryState(),
                    sender = senderState,
                )
            )
        }
    }

    private fun LocalChatGroup.getMemberToShow(loggedInMemberId: MemberId?): Flow<LocalMember> {
        return groupManager.getMembers(id).flatMapLatest { members ->
            val memberToShow = members.getOrThrow().first { it != loggedInMemberId }
            memberManager.getMember(memberToShow).map { it.getOrThrow() }
        }
    }

    private fun LocalMessage.toLoadedChatPreviewState(
        loggedInMemberId: MemberId?,
        community: LocalCommunity,
    ): Flow<LoadedChatPreviewState> {
        return community.getAvatarState(loggedInMemberId).flatMapLatest { communityAvatarState ->
            val senderState = when (senderId) {
                loggedInMemberId -> SentChatPreviewState.flow()
                else -> memberManager.getMember(senderId).mapLatest {
                    ReceivedCommunityChatPreviewState(it.getOrThrow().getAvatarState())
                }
            }

            senderState.mapLatest {
                LoadedChatPreviewState(
                    groupId = groupId,
                    avatar = communityAvatarState,
                    name = community.name,
                    content = ChatPreviewContentState(
                        message = content,
                        timestamp = timestamp.toLocalDateTime(),
                        delivery = delivery.toDeliveryState(),
                        sender = it,
                    ),
                )
            }
        }
    }

    private fun LocalCommunity.getAvatarState(loggedInMemberId: MemberId?): Flow<AvatarState> {
        if (avatarUrl != null) {
            return SingleAvatarState(
                url = avatarUrl,
                placeholder = name,
            ).flow()
        }

        return groupManager.getMembers(id).flatMapLatest { membersOutcome ->
            val memberIds = membersOutcome.getOrThrow()
            val avatars = memberIds.filter { it != loggedInMemberId }.take(2).map {
                memberManager.getMember(it).mapLatest { memberOutcome ->
                    memberOutcome.getOrThrow().getAvatarState()
                }
            }
            combine(avatars) {
                GroupAvatarState(
                    front = it[0],
                    back = it[1],
                )
            }
        }
    }

    private fun LocalMember.getAvatarState() = SingleAvatarState(
        url = avatarUrl,
        placeholder = username,
    )

    private fun LocalDelivery.toDeliveryState() = when (this) {
        LocalDelivery.Sending -> DeliveryState.Sending
        LocalDelivery.Sent -> DeliveryState.Sent
        LocalDelivery.Delivered -> DeliveryState.Delivered
        LocalDelivery.Read -> DeliveryState.Read
        LocalDelivery.Failed -> DeliveryState.Failed
    }

    private val conversationGroup = MutableStateFlow<GroupId?>(null)

    val conversation = authenticator.loggedInMember.flatMapLatest { loggedInMemberId ->
        conversationGroup.flatMapLatest conversationGroup@{ groupId ->
            if (groupId == null) return@conversationGroup flowOf(null)

            groupManager.getGroup(groupId).flatMapLatest { groupOutcome ->
                val direct = groupOutcome.getOrThrow() is LocalChatGroup

                messenger.getMessages(groupId).flatMapLatest { messagesOutcome ->
                    val messageIds = messagesOutcome.getOrThrow()
                    val messagesFlows = messageIds.map { messageId ->
                        messenger.getMessage(messageId).flatMapLatest { messageOutcome ->
                            messageOutcome.getOrThrow().toMessageEntry(loggedInMemberId, direct)
                        }
                    }
                    combineOrEmpty(messagesFlows) { entries ->
                        ConversationPaneState(
                            conversation = ConversationState(
                                groupId = groupId,
                                info = Loading,
                                entries = entries.toList(),
                                loading = true,
                            ),
                        )
                    }
                }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private fun LocalMessage.toMessageEntry(
        loggedInMemberId: MemberId?,
        direct: Boolean,
        mustSpace: Boolean = false,
    ): Flow<MessageEntry> {
        val senderState = when {
            loggedInMemberId == senderId -> SentMessageState.flow()
            direct -> ReceivedDirectMessageState.flow()
            else -> memberManager.getMember(senderId).mapLatest { memberOutcome ->
                val member = memberOutcome.getOrThrow()
                ReceivedCommunityMessageState(member.getAvatarState())
            }
        }

        return senderState.mapLatest {
            MessageState(
                id = id,
                content = content,
                timestamp = timestamp.toLocalDateTime(),
                delivery =  delivery.toDeliveryState(),
                groupPosition = GroupPosition.Middle,
                sender = it,
            ).toMessageEntry(mustSpace)
        }
    }

    private fun MessageState.toMessageEntry(mustSpace: Boolean = false) = MessageEntry(
        message = this,
        mustSpace = mustSpace,
    )
}