package com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state

import com.thebrownfoxx.neon.common.type.Loadable

data class ConversationState(
    val info: Loadable<ConversationInfoState>,
    val entries: Loadable<List<MessageListEntry>>,
)