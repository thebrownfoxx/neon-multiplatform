package com.thebrownfoxx.neon.client.application.ui.state

import com.thebrownfoxx.neon.client.application.ui.component.avatar.state.AvatarState
import com.thebrownfoxx.neon.client.application.ui.component.avatar.state.GroupAvatarState
import com.thebrownfoxx.neon.client.application.ui.component.avatar.state.SingleAvatarState
import com.thebrownfoxx.neon.client.model.LocalMember

fun LocalMember.getAvatarState() = SingleAvatarState(
    url = avatarUrl,
    placeholder = username,
)

fun List<LocalMember>.toAvatarState(): AvatarState? {
    if (isEmpty()) return null
    if (size == 1) return first().getAvatarState()
    return GroupAvatarState(
        front = this[0].getAvatarState(),
        back = this[1].getAvatarState(),
    )
}