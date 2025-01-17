package com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state

import com.thebrownfoxx.neon.common.type.Loadable
import com.thebrownfoxx.neon.common.type.id.GroupId

data class ConversationState(
    val groupId: GroupId = GroupId(),
    val info: Loadable<ConversationInfoState>,
    val entries: List<MessageListEntry>,
    val loadingEntries: Boolean,
)