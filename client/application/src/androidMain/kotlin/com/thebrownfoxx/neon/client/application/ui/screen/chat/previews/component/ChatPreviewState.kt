package com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.component

import com.thebrownfoxx.neon.client.application.ui.component.avatar.state.AvatarState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ChatPreviewContentState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ChatPreviewState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ChatPreviewStateValues
import com.thebrownfoxx.neon.common.type.Loaded
import com.thebrownfoxx.neon.common.type.Loading
import com.thebrownfoxx.neon.common.type.id.GroupId

fun loadedChatPreviewState(
    groupId: GroupId = GroupId(),
    avatar: AvatarState?,
    name: String?,
    content: ChatPreviewContentState?,
    emphasized: Boolean = false,
) = ChatPreviewState(
    values = Loaded(
        ChatPreviewStateValues(
            groupId = groupId,
            avatar = avatar,
            name = name,
            content = content,
            emphasized = emphasized,
        )
    )
)

fun loadingChatPreviewState() = ChatPreviewState(values = Loading)