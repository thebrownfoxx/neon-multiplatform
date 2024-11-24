package com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state

import com.thebrownfoxx.neon.client.application.ui.component.avatar.state.AvatarState
import com.thebrownfoxx.neon.common.type.id.GroupId

data class ConversationInfoState(
    val groupId: GroupId = GroupId(),
    val avatar: AvatarState?,
    val name: String?,
)