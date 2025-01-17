package com.thebrownfoxx.neon.client.application.ui.screen.chat.previews

import com.thebrownfoxx.neon.client.application.aggregator.GroupAggregator
import com.thebrownfoxx.neon.client.application.ui.component.avatar.state.AvatarState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ChatPreviewContentState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ChatPreviewHeader
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ChatPreviewHeaderValue
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ChatPreviewListItem
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ChatPreviewSenderState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ChatPreviewState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ChatPreviewStateId
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ChatPreviewStateValues
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ChatPreviewsState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ReceivedCommunityChatPreviewState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ReceivedDirectChatPreviewState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.SentChatPreviewState
import com.thebrownfoxx.neon.client.application.ui.state.getAvatarState
import com.thebrownfoxx.neon.client.application.ui.state.toAvatarState
import com.thebrownfoxx.neon.client.application.ui.state.toChatGroupName
import com.thebrownfoxx.neon.client.application.ui.state.toDeliveryState
import com.thebrownfoxx.neon.client.model.LocalChatGroup
import com.thebrownfoxx.neon.client.model.LocalCommunity
import com.thebrownfoxx.neon.client.model.LocalConversationPreviews
import com.thebrownfoxx.neon.client.model.LocalMessage
import com.thebrownfoxx.neon.client.service.Authenticator
import com.thebrownfoxx.neon.client.service.GroupManager
import com.thebrownfoxx.neon.client.service.MemberManager
import com.thebrownfoxx.neon.client.service.Messenger
import com.thebrownfoxx.neon.common.Logger
import com.thebrownfoxx.neon.common.data.Cache
import com.thebrownfoxx.neon.common.data.JobManager
import com.thebrownfoxx.neon.common.extension.flatListOf
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
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn

@OptIn(ThrowingApi::class, ExperimentalCoroutinesApi::class)
class ChatPreviewsStateHandler(
    authenticator: Authenticator,
    private val groupManager: GroupManager,
    private val memberManager: MemberManager,
    private val messenger: Messenger,
    private val idMap: MutableMap<GroupId, ChatPreviewStateId>,
    lastVisibleGroupId: Flow<GroupId?>,
    externalScope: CoroutineScope,
    private val logger: Logger,
) {
    private val previewValuesMirrorJobManager = JobManager<GroupId>(externalScope)
    private val previewValuesCache = Cache<GroupId, Loadable<ChatPreviewStateValues>>(externalScope)

    private val groupAggregator = GroupAggregator(groupManager, memberManager)

    private val initialState = ChatPreviewsState(
        listItems = List(20) { ChatPreviewState(values = Loading) },
        ready = false,
    )

    val previews = authenticator.loggedInMemberId.flatMapLatest { loggedInMemberId ->
        lastVisibleGroupId.flatMapLatest { lastVisiblePreviewId ->
            getPreviews(loggedInMemberId, lastVisiblePreviewId)
        }
    }
        .catch { logger.logError(it) }
        .stateIn(externalScope, SharingStarted.Eagerly, initialState)

    private fun getPreviews(
        loggedInMemberId: MemberId?,
        lastVisibleGroupId: GroupId?,
    ): Flow<ChatPreviewsState> {
        return messenger.conversationPreviews.flatMapLatest { previewsOutcome ->
            val previews = previewsOutcome.getOrThrow()
            previews.mapToInitialStateIds()

            val lastVisiblePreviewIndex = previews.toFlatList()
                .indexOfFirst { it.groupId == lastVisibleGroupId }
            val lastIndexToLoad = lastVisiblePreviewIndex + 20

            combine(
                previews.getNudgedListItems(loggedInMemberId, lastIndexToLoad),
                previews.getUnreadListItems(loggedInMemberId, lastIndexToLoad),
                previews.getReadListItems(loggedInMemberId, lastIndexToLoad),
            ) { nudged, unread, read ->
                ChatPreviewsState(
                    listItems = flatListOf(nudged, unread, read),
                    ready = true,
                )
            }
        }
    }

    private fun LocalConversationPreviews.toFlatList(): List<LocalMessage> {
        return flatListOf(nudgedPreviews, unreadPreviews, readPreviews)
    }

    private fun LocalConversationPreviews.mapToInitialStateIds() {
        initialState.listItems.forEachIndexed { index, state ->
            if (state !is ChatPreviewState || state.id in idMap.values) return@forEachIndexed
            val groupId = toFlatList().getOrNull(index)?.groupId
                ?: return@forEachIndexed
            idMap[groupId] = state.id
        }
    }

    private fun LocalConversationPreviews.getNudgedListItems(
        loggedInMemberId: MemberId?,
        lastIndexToLoad: Int,
    ): Flow<List<ChatPreviewListItem>> {
        return nudgedPreviews.mapIndexed { index, item ->
            item.toChatPreviewState(
                loggedInMemberId = loggedInMemberId,
                mustBeLoaded = index <= lastIndexToLoad,
            )
        }.combineToListItems(ChatPreviewHeaderValue.NudgedConversations)
    }

    private fun LocalConversationPreviews.getUnreadListItems(
        loggedInMemberId: MemberId?,
        lastIndexToLoad: Int,
    ): Flow<List<ChatPreviewListItem>> {
        val headerValue = when {
            readPreviews.isEmpty() -> ChatPreviewHeaderValue.Conversations
            else -> ChatPreviewHeaderValue.UnreadConversations
        }
        return unreadPreviews.mapIndexed { index, item ->
            val cumulativeIndex = index + nudgedPreviews.size
            item.toChatPreviewState(
                loggedInMemberId = loggedInMemberId,
                mustBeLoaded = cumulativeIndex <= lastIndexToLoad,
            )
        }.combineToListItems(headerValue)
    }

    private fun LocalConversationPreviews.getReadListItems(
        loggedInMemberId: MemberId?,
        lastIndexToLoad: Int,
    ): Flow<List<ChatPreviewListItem>> {
        val headerValue = when {
            unreadPreviews.isEmpty() -> ChatPreviewHeaderValue.Conversations
            else -> ChatPreviewHeaderValue.ReadConversations
        }
        return readPreviews.mapIndexed { index, item ->
            val cumulativeIndex =
                index + nudgedPreviews.size + unreadPreviews.size
            item.toChatPreviewState(
                loggedInMemberId = loggedInMemberId,
                mustBeLoaded = cumulativeIndex <= lastIndexToLoad,
            )
        }.combineToListItems(headerValue)
    }

    private fun List<Flow<ChatPreviewState>>.combineToListItems(
        headerValue: ChatPreviewHeaderValue,
    ): Flow<List<ChatPreviewListItem>> {
        return combineOrEmpty(this) {
            val list = it.toList()
            flatListOf(list.toHeaderFlatSubList(headerValue), list)
        }
    }

    private fun List<ChatPreviewState>.toHeaderFlatSubList(
        value: ChatPreviewHeaderValue,
    ): List<ChatPreviewHeader> {
        if (isEmpty()) return emptyList()
        val header = ChatPreviewHeader(
            value = value,
            showPlaceholder = !any { it.values is Loaded },
        )
        return listOf(header)
    }

    private fun LocalMessage.toChatPreviewState(
        loggedInMemberId: MemberId?,
        mustBeLoaded: Boolean,
    ): Flow<ChatPreviewState> {
        return toLoadableChatPreviewState(loggedInMemberId, mustBeLoaded).mapLatest { values ->
            ChatPreviewState(
                id = idMap.getOrPut(groupId) { ChatPreviewStateId() },
                values = values,
            )
        }
    }

    private fun LocalMessage.toLoadableChatPreviewState(
        loggedInMemberId: MemberId?,
        mustBeLoaded: Boolean,
    ): Flow<Loadable<ChatPreviewStateValues>> {
        return previewValuesCache.getOrInitialize(groupId) {
            emit(Loading)
        }.also {
            if (!mustBeLoaded) return@also
            previewValuesMirrorJobManager[groupId] = {
                val values = toChatPreviewStateValues(loggedInMemberId)
                previewValuesCache.mirror(values) { Cache.Entry(groupId, Loaded(it)) }
            }
        }
    }

    private fun LocalMessage.toChatPreviewStateValues(
        loggedInMemberId: MemberId?,
    ): Flow<ChatPreviewStateValues> {
        return groupManager.getGroup(groupId).flatMapLatest { groupOutcome ->
            when (val group = groupOutcome.getOrThrow()) {
                is LocalChatGroup -> toChatPreviewStateValues(loggedInMemberId, group)
                is LocalCommunity -> toChatPreviewStateValues(loggedInMemberId, group)
            }
        }
    }

    private fun LocalMessage.toChatPreviewStateValues(
        loggedInMemberId: MemberId?,
        chatGroup: LocalChatGroup,
    ): Flow<ChatPreviewStateValues> {
        val sender = when (senderId) {
            loggedInMemberId -> SentChatPreviewState
            else -> ReceivedDirectChatPreviewState
        }
        return groupAggregator.getMembers(
            groupId = chatGroup.id,
            excludedIds = listOfNotNull(loggedInMemberId),
        ).mapLatest { members ->
            toChatPreviewStateValues(
                avatarState = members.toAvatarState(),
                name = members.toChatGroupName(),
                sender = sender,
            )
        }
    }

    private fun LocalMessage.toChatPreviewStateValues(
        loggedInMemberId: MemberId?,
        community: LocalCommunity,
    ): Flow<ChatPreviewStateValues> {
        return groupAggregator.getCommunityAvatar(
            loggedInMemberId,
            community,
        ).flatMapLatest { avatarState ->
            when (senderId) {
                loggedInMemberId -> SentChatPreviewState.flow()
                else -> getReceivedCommunitySender(senderId)
            }.mapLatest { sender ->
                toChatPreviewStateValues(avatarState, community.name, sender)
            }
        }
    }

    private fun getReceivedCommunitySender(
        senderId: MemberId,
    ): Flow<ReceivedCommunityChatPreviewState> {
        return memberManager.getMember(senderId).mapLatest {
            ReceivedCommunityChatPreviewState(it.getOrThrow().getAvatarState())
        }
    }

    private fun LocalMessage.toChatPreviewStateValues(
        avatarState: AvatarState?,
        name: String?,
        sender: ChatPreviewSenderState,
    ) = ChatPreviewStateValues(
        groupId = groupId,
        avatar = avatarState,
        name = name,
        content = ChatPreviewContentState(
            message = content,
            timestamp = timestamp.toLocalDateTime(),
            delivery = delivery.toDeliveryState(),
            sender = sender,
        ),
    )
}