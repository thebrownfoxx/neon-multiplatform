package com.thebrownfoxx.neon.client.application.ui.screen.conversations.state

import com.thebrownfoxx.neon.client.application.ui.component.avatar.state.AvatarState
import com.thebrownfoxx.neon.common.model.GroupId

data class ConversationPreviewState(
    val groupId: GroupId = GroupId(),
    val avatar: AvatarState?,
    val name: String?,
    val content: PreviewContentState?,
)