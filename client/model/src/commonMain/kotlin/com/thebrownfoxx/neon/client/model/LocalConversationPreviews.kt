package com.thebrownfoxx.neon.client.model

data class LocalConversationPreviews(
    val nudgedPreviews: List<LocalMessage>,
    val unreadPreviews: List<LocalMessage>,
    val readPreviews: List<LocalMessage>,
)