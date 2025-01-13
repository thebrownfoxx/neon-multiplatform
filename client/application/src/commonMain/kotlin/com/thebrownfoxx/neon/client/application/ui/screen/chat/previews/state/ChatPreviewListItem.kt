package com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state

import com.thebrownfoxx.neon.client.application.ui.component.avatar.state.AvatarState
import com.thebrownfoxx.neon.common.type.Loadable
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.Id
import com.thebrownfoxx.neon.common.type.id.Uuid

data class ChatPreviewState(
    val id: ChatPreviewStateId = ChatPreviewStateId(),
    val values: Loadable<ChatPreviewStateValues>,
) : ChatPreviewListItem {
    override val key = id.value
}

data class ChatPreviewStateId(override val uuid: Uuid = Uuid()) : Id

data class ChatPreviewStateValues(
    val groupId: GroupId = GroupId(),
    val avatar: AvatarState?,
    val name: String?,
    val content: ChatPreviewContentState?,
    val emphasized: Boolean = false,
)

data class ChatPreviewHeader(
    val value: ChatPreviewHeaderValue,
    val showPlaceholder: Boolean = false,
) : ChatPreviewListItem {
    override val key = value.name
}

enum class ChatPreviewHeaderValue {
    NudgedConversations,
    UnreadConversations,
    ReadConversations,
    Conversations,
}

sealed interface ChatPreviewListItem {
    val key: String
}