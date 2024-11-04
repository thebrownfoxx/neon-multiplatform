package com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thebrownfoxx.neon.client.application.ui.component.Spacer
import com.thebrownfoxx.neon.client.application.ui.extension.bottomDp
import com.thebrownfoxx.neon.client.application.ui.extension.horizontal
import com.thebrownfoxx.neon.client.application.ui.extension.topDp
import com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.state.GroupPosition

@Composable
fun DirectMessageListLoader(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    Column(
        verticalArrangement = Arrangement.Bottom,
        modifier = modifier.padding(contentPadding.horizontal),
    ) {
        Spacer(height = contentPadding.topDp)

        SentMessageLoader(text = "The quick brown foxx!!", groupPosition = GroupPosition.First)
        Spacer(height = 4.dp)
        SentMessageLoader(text = "Skibidi skibidi", groupPosition = GroupPosition.Last)

        Spacer(height = 16.dp)

        ReceivedMessageLoader(text = "Dop dop dop", groupPosition = GroupPosition.Alone)

        Spacer(height = 16.dp)

        SentMessageLoader(text = "fax", groupPosition = GroupPosition.First)

        Spacer(height = 16.dp)

        ReceivedMessageLoader(text = "What the sigma?", groupPosition = GroupPosition.First)
        Spacer(height = 4.dp)
        ReceivedMessageLoader(text = "sigma?", groupPosition = GroupPosition.Last)

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
) {
    ReceivedMessageLayout(modifier = Modifier.fillMaxWidth()) {
        ReceivedMessageBubbleLoader(
            text = text,
            groupPosition = groupPosition,
        )
    }
}