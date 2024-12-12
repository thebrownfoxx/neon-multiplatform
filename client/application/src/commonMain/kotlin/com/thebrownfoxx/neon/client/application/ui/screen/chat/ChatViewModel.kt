package com.thebrownfoxx.neon.client.application.ui.screen.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thebrownfoxx.neon.client.application.ui.component.avatar.state.AvatarState
import com.thebrownfoxx.neon.client.application.ui.component.avatar.state.GroupAvatarState
import com.thebrownfoxx.neon.client.application.ui.component.avatar.state.SingleAvatarState
import com.thebrownfoxx.neon.client.application.ui.component.delivery.state.DeliveryState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.ConversationPaneState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ChatPreviewContentState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ChatPreviewState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ChatPreviewsState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.LoadedChatPreviewState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.LoadingChatPreviewState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ReceivedCommunityState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ReceivedDirectState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.SentState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.state.ConversationDummy
import com.thebrownfoxx.neon.client.model.LocalChatGroup
import com.thebrownfoxx.neon.client.model.LocalCommunity
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
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.outcome.map.getOrThrow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

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

    // TODO: Change this to index-based so we can preload close items way before they get scrolled to
    private val previewsToLoad = MutableStateFlow(emptySet<GroupId>())

    val chatPreviews = messenger.conversationPreviews.flatMapLatest { conversationPreviewsOutcome ->
        previewsToLoad.flatMapLatest { previewsToLoad ->
            val conversationPreviews = conversationPreviewsOutcome.getOrThrow()

            val nudgedPreviews = conversationPreviews.nudgedPreviews
                .map { it.toChatPreviewState(previewsToLoad) }

            val unreadPreviews = conversationPreviews.unreadPreviews
                .map { it.toChatPreviewState(previewsToLoad) }

            val readPreviews = conversationPreviews.readPreviews
                .map { it.toChatPreviewState(previewsToLoad) }

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

    private val _conversation = MutableStateFlow<ConversationPaneState?>(null)
    val conversation = _conversation.asStateFlow()

    fun onConversationClick(chatPreviewState: ChatPreviewState) {
        val dummy = ConversationDummy.ConversationPaneState
        _conversation.value = dummy.copy(
            conversation = dummy.conversation.copy(
                info = dummy.conversation.info.map {
                    it.copy(groupId = chatPreviewState.groupId)
                }
            )
        )
    }

    fun onLoadPreview(chatPreviewState: ChatPreviewState) {
        previewsToLoad.update { it + chatPreviewState.groupId }
    }

    fun onConversationClose() {
        _conversation.value = null
    }

    private fun LocalMessage.toChatPreviewState(
        previewsToLoad: Set<GroupId>,
    ): Flow<ChatPreviewState> {
        return when {
            groupId in previewsToLoad -> toChatPreviewState()
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
            loggedInMemberId -> SentState
            else -> ReceivedDirectState
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
                loggedInMemberId -> SentState.flow()
                else -> memberManager.getMember(senderId).mapLatest {
                    ReceivedCommunityState(it.getOrThrow().getAvatarState())
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
}