package com.thebrownfoxx.neon.client.model

import com.thebrownfoxx.neon.common.extension.flatListOf

data class LocalChatPreviews(
    val nudgedPreviews: List<LocalMessage>,
    val unreadPreviews: List<LocalMessage>,
    val readPreviews: List<LocalMessage>,
) {
    fun toFlatList() = flatListOf(nudgedPreviews, unreadPreviews, readPreviews)
}