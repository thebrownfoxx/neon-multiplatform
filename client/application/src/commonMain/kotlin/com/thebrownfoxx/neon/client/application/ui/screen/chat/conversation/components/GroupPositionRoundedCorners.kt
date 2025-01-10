package com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.components

import com.thebrownfoxx.neon.client.application.ui.extension.Corner
import com.thebrownfoxx.neon.client.application.ui.extension.RoundedCorners
import com.thebrownfoxx.neon.client.application.ui.extension.Side
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.GroupPosition

fun GroupPosition.toSentMessageBubbleRoundedCorners() = when (this) {
    GroupPosition.Alone, GroupPosition.Last ->
        RoundedCorners(Corner.TopStart, Corner.TopEnd, Corner.BottomStart)

    else -> RoundedCorners(Side.Start)
}

fun GroupPosition.toReceivedMessageBubbleRoundedCorners() = when (this) {
    GroupPosition.Alone, GroupPosition.First ->
        RoundedCorners(Corner.TopEnd, Corner.BottomEnd, Corner.BottomStart)

    else -> RoundedCorners(Side.End)
}