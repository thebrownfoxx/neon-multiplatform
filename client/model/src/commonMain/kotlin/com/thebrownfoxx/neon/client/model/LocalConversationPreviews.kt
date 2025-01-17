package com.thebrownfoxx.neon.client.model

data class LocalChatPreviews(
    val nudgedPreviews: List<LocalMessage>,
    val unreadPreviews: List<LocalMessage>,
    val readPreviews: List<LocalMessage>,
)