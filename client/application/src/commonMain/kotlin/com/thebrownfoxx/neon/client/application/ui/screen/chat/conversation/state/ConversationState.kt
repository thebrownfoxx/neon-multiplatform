package com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state

import com.thebrownfoxx.neon.client.application.ui.component.avatar.state.AvatarState
import com.thebrownfoxx.neon.common.model.GroupId
import com.thebrownfoxx.neon.common.type.Loadable

data class ConversationState(
    val groupId: GroupId = GroupId(),
    val avatar: AvatarState?,
    val name: String?,
    val entries: Loadable<List<MessageListEntry>>,
)