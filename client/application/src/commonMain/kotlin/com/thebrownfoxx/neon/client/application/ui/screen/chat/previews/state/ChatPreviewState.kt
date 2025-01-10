package com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state

import com.thebrownfoxx.neon.client.application.ui.component.avatar.state.AvatarState
import com.thebrownfoxx.neon.common.type.id.GroupId

sealed interface ChatPreviewState {
    val groupId: GroupId
}

data class LoadingChatPreviewState(
    override val groupId: GroupId = GroupId(),
) : ChatPreviewState

data class LoadedChatPreviewState(
    override val groupId: GroupId = GroupId(),
    val avatar: AvatarState?,
    val name: String?,
    val content: ChatPreviewContentState?,
    val emphasized: Boolean = false,
) : ChatPreviewState