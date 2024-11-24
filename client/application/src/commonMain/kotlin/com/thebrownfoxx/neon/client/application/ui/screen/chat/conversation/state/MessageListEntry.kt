package com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state

import com.thebrownfoxx.neon.common.type.id.Id
import com.thebrownfoxx.neon.common.type.id.Uuid
import kotlinx.datetime.LocalDateTime

sealed interface MessageListEntry {
    val id: MessageListEntryId
    val mustSpace: Boolean
}

data class ChunkTimestamp(
    override val id: MessageListEntryId = MessageListEntryId(),
    val timestamp: LocalDateTime,
) : MessageListEntry {
    override val mustSpace = true
}

data class MessageEntry(
    override val id: MessageListEntryId = MessageListEntryId(),
    val message: MessageState,
    override val mustSpace: Boolean,
) : MessageListEntry

data class MessageListEntryId(override val uuid: Uuid = Uuid()) : Id