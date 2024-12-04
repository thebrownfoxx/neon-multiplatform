package com.thebrownfoxx.neon.client.model

data class LocalConversationPreviews(
    val nudgedPreviews: Set<LocalMessage>,
    val unreadPreviews: Set<LocalMessage>,
    val readPreviews: Set<LocalMessage>,
)