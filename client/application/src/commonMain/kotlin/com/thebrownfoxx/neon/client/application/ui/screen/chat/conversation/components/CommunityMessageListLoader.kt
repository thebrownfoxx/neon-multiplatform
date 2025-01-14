package com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thebrownfoxx.neon.client.application.ui.component.Spacer
import com.thebrownfoxx.neon.client.application.ui.component.avatar.AvatarSize
import com.thebrownfoxx.neon.client.application.ui.component.common.AnimatedVisibility
import com.thebrownfoxx.neon.client.application.ui.component.common.ExpandAxis
import com.thebrownfoxx.neon.client.application.ui.component.loader.MediumAvatarLoader
import com.thebrownfoxx.neon.client.application.ui.extension.bottomDp
import com.thebrownfoxx.neon.client.application.ui.extension.horizontal
import com.thebrownfoxx.neon.client.application.ui.extension.topDp
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.GroupPosition

@Composable
fun MessageListLoader(
    isCommunity: Boolean,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    Column(
        verticalArrangement = Arrangement.Bottom,
        modifier = modifier.padding(contentPadding.horizontal),
    ) {
        Spacer(height = contentPadding.topDp)

        SentMessageLoader(text = "The quick brown foxx!!", groupPosition = GroupPosition.Last)
        Spacer(height = 4.dp)
        SentMessageLoader(text = "Skibidi skibidi", groupPosition = GroupPosition.First)

        Spacer(height = 16.dp)

        ReceivedMessageLoader(
            text = "Dop dop dop",
            groupPosition = GroupPosition.Alone,
            isCommunity = isCommunity,
        )

        Spacer(height = 16.dp)

        SentMessageLoader(text = "fax", groupPosition = GroupPosition.Last)

        Spacer(height = 16.dp)

        ReceivedMessageLoader(
            text = "What the sigma?",
            groupPosition = GroupPosition.Last,
            isCommunity = isCommunity,
        )
        Spacer(height = 4.dp)
        ReceivedMessageLoader(
            text = "sigma?",
            groupPosition = GroupPosition.First,
            isCommunity = isCommunity,
        )

        Spacer(height = contentPadding.bottomDp)
    }
}

@Composable
private fun SentMessageLoader(
    text: String,
    groupPosition: GroupPosition,
) {
    SentMessageLayout(modifier = Modifier.fillMaxWidth()) {
        SentMessageBubbleLoader(
            text = text,
            groupPosition = groupPosition,
        )
    }
}

@Composable
private fun ReceivedMessageLoader(
    text: String,
    groupPosition: GroupPosition,
    isCommunity: Boolean,
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxWidth(0.8f),
        ) {
            AnimatedVisibility(
                visible = isCommunity,
                expandAxis = ExpandAxis.Both,
            ) {
                AvatarLoader(groupPosition = groupPosition)
            }
            ReceivedMessageBubbleLoader(
                text = text,
                groupPosition = groupPosition,
            )
        }
    }
}

@Composable
private fun AvatarLoader(groupPosition: GroupPosition) {
    Row {
        when (groupPosition) {
            GroupPosition.Last, GroupPosition.Alone -> MediumAvatarLoader()
            else -> Spacer(width = AvatarSize.Medium.dp)
        }
        Spacer(width = 16.dp)
    }
}