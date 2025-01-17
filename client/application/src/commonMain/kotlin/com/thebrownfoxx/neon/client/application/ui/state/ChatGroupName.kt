package com.thebrownfoxx.neon.client.application.ui.state

import com.thebrownfoxx.neon.client.model.LocalMember

fun List<LocalMember>.toChatGroupName(): String? {
    if (isEmpty()) return null
    if (size == 1) return first().username
    return joinToString(separator = ", ") { it.username }
}