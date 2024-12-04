package com.thebrownfoxx.neon.client.converter

import com.thebrownfoxx.neon.client.model.LocalMember
import com.thebrownfoxx.neon.server.model.Member

fun Member.toLocalMember() = LocalMember(
    id = id,
    username = username,
    avatarUrl = avatarUrl,
)