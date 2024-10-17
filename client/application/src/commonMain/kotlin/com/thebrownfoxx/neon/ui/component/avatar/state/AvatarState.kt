package com.thebrownfoxx.neon.ui.component.avatar.state

import com.thebrownfoxx.neon.common.type.Url

sealed interface AvatarState

data class SingleAvatarState(
    val url: Url?,
    val placeholder: String,
) : AvatarState

data class GroupAvatarState(
    val front: SingleAvatarState?,
    val back: SingleAvatarState?,
) : AvatarState