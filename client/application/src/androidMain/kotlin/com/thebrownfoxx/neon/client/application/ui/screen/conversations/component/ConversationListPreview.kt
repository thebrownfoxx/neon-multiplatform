package com.thebrownfoxx.neon.client.application.ui.screen.conversations.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.thebrownfoxx.neon.client.application.ui.component.avatar.state.GroupAvatarState
import com.thebrownfoxx.neon.client.application.ui.component.avatar.state.SingleAvatarState
import com.thebrownfoxx.neon.client.application.ui.component.delivery.state.DeliveryState
import com.thebrownfoxx.neon.client.application.ui.screen.conversations.state.ConversationPreviewState
import com.thebrownfoxx.neon.client.application.ui.screen.conversations.state.PreviewContentState
import com.thebrownfoxx.neon.client.application.ui.screen.conversations.state.ReceivedCommunityState
import com.thebrownfoxx.neon.client.application.ui.screen.conversations.state.ReceivedDirectState
import com.thebrownfoxx.neon.client.application.ui.screen.conversations.state.SentState
import com.thebrownfoxx.neon.client.application.ui.theme.NeonTheme
import com.thebrownfoxx.neon.common.extension.ago
import com.thebrownfoxx.neon.common.extension.toLocalDateTime
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

private val oconGroupAvatar = GroupAvatarState(
    front = SingleAvatarState(
        url = null,
        placeholder = "oconman",
    ),
    back = SingleAvatarState(
        url = null,
        placeholder = "logie_bear"
    ),
)

private val nudgedConversations = listOf(
    ConversationPreviewState(
        avatar = SingleAvatarState(
            url = null,
            placeholder = "AndreaStella",
        ),
        name = "AndreaStella",
        content = PreviewContentState(
            message = "where the feet pics",
            timestamp = 1.days.ago.toLocalDateTime(),
            delivery = null,
            senderState = ReceivedDirectState,
        ),
    ),
    ConversationPreviewState(
        avatar = SingleAvatarState(
            url = null,
            placeholder = "will_joseph",
        ),
        name = "will_joseph",
        content = PreviewContentState(
            message = "feet pics or im sabotaging your next race",
            timestamp = 2.days.ago.toLocalDateTime(),
            delivery = null,
            senderState = ReceivedDirectState,
        ),
    ),
).sortedByDescending { it.content?.timestamp }

private val unreadConversations = listOf(
    ConversationPreviewState(
        avatar = SingleAvatarState(
            url = null,
            placeholder = "McLaren",
        ),
        name = "McLaren",
        content = PreviewContentState(
            message = "I'm sorry Lando",
            timestamp = 14.minutes.ago.toLocalDateTime(),
            delivery = null,
            senderState = ReceivedCommunityState(
                senderAvatar = SingleAvatarState(
                    url = null,
                    placeholder = "oscoala",
                ),
            ),
        ),
    ),
    ConversationPreviewState(
        avatar = SingleAvatarState(
            url = null,
            placeholder = "carlito",
        ),
        name = "carlito",
        content = PreviewContentState(
            message = "you ready to get freaky? 🤤",
            timestamp = 5.minutes.ago.toLocalDateTime(),
            delivery = null,
            senderState = ReceivedDirectState,
        ),
    ),
).sortedByDescending { it.content?.timestamp }

private val readConversations = listOf(
    ConversationPreviewState(
        avatar = SingleAvatarState(
            url = null,
            placeholder = "SharlLeclair",
        ),
        name = "SharlLeclair",
        content = PreviewContentState(
            message = "GET AWAY FROM CARLOS",
            timestamp = 7.minutes.ago.toLocalDateTime(),
            delivery = DeliveryState.Read,
            senderState = SentState,
        ),
    ),
    ConversationPreviewState(
        avatar = oconGroupAvatar,
        name = "Cool Ones",
        content = PreviewContentState(
            message = "STOP ADDING ME TO COMMUNITIES OCON!",
            timestamp = 18.minutes.ago.toLocalDateTime(),
            delivery = DeliveryState.Failed,
            senderState = SentState,
        ),
    ),
    ConversationPreviewState(
        avatar = SingleAvatarState(
            url = null,
            placeholder = "MaxFewtrell",
        ),
        name = "MaxFewtrell",
        content = PreviewContentState(
            message = "im naked rn 😉",
            timestamp = 2.minutes.ago.toLocalDateTime(),
            delivery = DeliveryState.Reacted("🤤"),
            senderState = SentState,
        ),
    ),
    ConversationPreviewState(
        avatar = SingleAvatarState(
            url = null,
            placeholder = "logie_bear",
        ),
        name = "logie_bear",
        content = PreviewContentState(
            message = "yea im blocking ocon",
            timestamp = 3.minutes.ago.toLocalDateTime(),
            delivery = DeliveryState.Delivered,
            senderState = SentState,
        ),
    ),
    ConversationPreviewState(
        avatar = SingleAvatarState(
            url = null,
            placeholder = "oscoala",
        ),
        name = "oscoala",
        content = PreviewContentState(
            message = "I'm sorry Lando.",
            timestamp = 20.minutes.ago.toLocalDateTime(),
            delivery = null,
            senderState = ReceivedDirectState,
        ),
    ),
    ConversationPreviewState(
        avatar = oconGroupAvatar,
        name = "Cool Doods",
        content = PreviewContentState(
            message = "wtf is this ocon",
            timestamp = 30.minutes.ago.toLocalDateTime(),
            delivery = DeliveryState.Sent,
            senderState = SentState,
        ),
    ),
    ConversationPreviewState(
        avatar = oconGroupAvatar,
        name = "Awesome Doods",
        content = PreviewContentState(
            message = "hello? what is this?",
            timestamp = 45.minutes.ago.toLocalDateTime(),
            delivery = DeliveryState.Sent,
            senderState = SentState,
        ),
    ),
    ConversationPreviewState(
        avatar = oconGroupAvatar,
        name = "Awesome Doods",
        content = PreviewContentState(
            message = "Esteban, what is this? 🦅🦅",
            timestamp = 48.minutes.ago.toLocalDateTime(),
            delivery = null,
            senderState = ReceivedCommunityState(
                senderAvatar = SingleAvatarState(
                    url = null,
                    placeholder = "logie_bear",
                ),
            ),
        ),
    ),
    ConversationPreviewState(
        avatar = oconGroupAvatar,
        name = "Awesome Doods",
        content = null,
    ),
    ConversationPreviewState(
        avatar = oconGroupAvatar,
        name = "Awesome Doods",
        content = null,
    ),
    ConversationPreviewState(
        avatar = oconGroupAvatar,
        name = "Awesome Doods",
        content = null,
    ),
).sortedByDescending { it.content?.timestamp }

@Preview
@Composable
private fun Preview() {
    NeonTheme {
        ConversationPreviews(
            nudgedConversations = nudgedConversations,
            unreadConversations = unreadConversations,
            readConversations = readConversations,
            onConversationClick = {},
            onLoadMore = {},
        )
    }
}

@Preview
@Composable
private fun NoCatchupPreview() {
    NeonTheme {
        ConversationPreviews(
            nudgedConversations = emptyList(),
            unreadConversations = unreadConversations,
            readConversations = readConversations,
            onConversationClick = {},
            onLoadMore = {},
        )
    }
}

@Preview
@Composable
private fun NoUnreadMessagesPreview() {
    NeonTheme {
        ConversationPreviews(
            nudgedConversations = emptyList(),
            unreadConversations = emptyList(),
            readConversations = readConversations,
            onConversationClick = {},
            onLoadMore = {},
        )
    }
}

@Preview
@Composable
private fun NoReadMessagesPreview() {
    NeonTheme {
        ConversationPreviews(
            nudgedConversations = emptyList(),
            unreadConversations = unreadConversations,
            readConversations = emptyList(),
            onConversationClick = {},
            onLoadMore = {},
        )
    }
}