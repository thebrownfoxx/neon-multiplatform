package com.thebrownfoxx.neon.client.application.ui.screen.chat.previews

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.thebrownfoxx.neon.client.application.ui.component.avatar.state.GroupAvatarState
import com.thebrownfoxx.neon.client.application.ui.component.avatar.state.SingleAvatarState
import com.thebrownfoxx.neon.client.application.ui.component.delivery.state.DeliveryState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ChatPreviewState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ChatPreviewContentState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ChatPreviewsEventHandler
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.LoadedChatPreviewsState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ReceivedCommunityState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.ReceivedDirectState
import com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.state.SentState
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
    ChatPreviewState(
        avatar = SingleAvatarState(
            url = null,
            placeholder = "AndreaStella",
        ),
        name = "AndreaStella",
        content = ChatPreviewContentState(
            message = "where the feet pics",
            timestamp = 1.days.ago.toLocalDateTime(),
            delivery = null,
            senderState = ReceivedDirectState,
        ),
    ),
    ChatPreviewState(
        avatar = SingleAvatarState(
            url = null,
            placeholder = "will_joseph",
        ),
        name = "will_joseph",
        content = ChatPreviewContentState(
            message = "feet pics or im sabotaging your next race",
            timestamp = 2.days.ago.toLocalDateTime(),
            delivery = null,
            senderState = ReceivedDirectState,
        ),
    ),
).sortedByDescending { it.content?.timestamp }

private val unreadConversations = listOf(
    ChatPreviewState(
        avatar = SingleAvatarState(
            url = null,
            placeholder = "McLaren",
        ),
        name = "McLaren",
        content = ChatPreviewContentState(
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
    ChatPreviewState(
        avatar = SingleAvatarState(
            url = null,
            placeholder = "carlito",
        ),
        name = "carlito",
        content = ChatPreviewContentState(
            message = "you ready to get freaky? 🤤",
            timestamp = 5.minutes.ago.toLocalDateTime(),
            delivery = null,
            senderState = ReceivedDirectState,
        ),
    ),
).sortedByDescending { it.content?.timestamp }

private val readConversations = listOf(
    ChatPreviewState(
        avatar = SingleAvatarState(
            url = null,
            placeholder = "SharlLeclair",
        ),
        name = "SharlLeclair",
        content = ChatPreviewContentState(
            message = "GET AWAY FROM CARLOS",
            timestamp = 7.minutes.ago.toLocalDateTime(),
            delivery = DeliveryState.Read,
            senderState = SentState,
        ),
    ),
    ChatPreviewState(
        avatar = oconGroupAvatar,
        name = "Cool Ones",
        content = ChatPreviewContentState(
            message = "STOP ADDING ME TO COMMUNITIES OCON!",
            timestamp = 18.minutes.ago.toLocalDateTime(),
            delivery = DeliveryState.Failed,
            senderState = SentState,
        ),
    ),
    ChatPreviewState(
        avatar = SingleAvatarState(
            url = null,
            placeholder = "MaxFewtrell",
        ),
        name = "MaxFewtrell",
        content = ChatPreviewContentState(
            message = "im naked rn 😉",
            timestamp = 2.minutes.ago.toLocalDateTime(),
            delivery = DeliveryState.Reacted("🤤"),
            senderState = SentState,
        ),
    ),
    ChatPreviewState(
        avatar = SingleAvatarState(
            url = null,
            placeholder = "logie_bear",
        ),
        name = "logie_bear",
        content = ChatPreviewContentState(
            message = "yea im blocking ocon",
            timestamp = 3.minutes.ago.toLocalDateTime(),
            delivery = DeliveryState.Delivered,
            senderState = SentState,
        ),
    ),
    ChatPreviewState(
        avatar = SingleAvatarState(
            url = null,
            placeholder = "oscoala",
        ),
        name = "oscoala",
        content = ChatPreviewContentState(
            message = "I'm sorry Lando.",
            timestamp = 20.minutes.ago.toLocalDateTime(),
            delivery = null,
            senderState = ReceivedDirectState,
        ),
    ),
    ChatPreviewState(
        avatar = oconGroupAvatar,
        name = "Cool Doods",
        content = ChatPreviewContentState(
            message = "wtf is this ocon",
            timestamp = 30.minutes.ago.toLocalDateTime(),
            delivery = DeliveryState.Sent,
            senderState = SentState,
        ),
    ),
    ChatPreviewState(
        avatar = oconGroupAvatar,
        name = "Awesome Doods",
        content = ChatPreviewContentState(
            message = "hello? what is this?",
            timestamp = 45.minutes.ago.toLocalDateTime(),
            delivery = DeliveryState.Sent,
            senderState = SentState,
        ),
    ),
    ChatPreviewState(
        avatar = oconGroupAvatar,
        name = "Awesome Doods",
        content = ChatPreviewContentState(
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
    ChatPreviewState(
        avatar = oconGroupAvatar,
        name = "Awesome Doods",
        content = null,
    ),
    ChatPreviewState(
        avatar = oconGroupAvatar,
        name = "Awesome Doods",
        content = null,
    ),
    ChatPreviewState(
        avatar = oconGroupAvatar,
        name = "Awesome Doods",
        content = null,
    ),
).sortedByDescending { it.content?.timestamp }

@Preview
@Composable
private fun Preview() {
    NeonTheme {
        ChatPreviews(
            state = LoadedChatPreviewsState(
                nudgedConversations = nudgedConversations,
                unreadConversations = unreadConversations,
                readConversations = readConversations,
            ),
            eventHandler = ChatPreviewsEventHandler.Blank,
        )
    }
}

@Preview
@Composable
private fun NoNudgedPreview() {
    NeonTheme {
        ChatPreviews(
            state = LoadedChatPreviewsState(
                nudgedConversations = emptyList(),
                unreadConversations = unreadConversations,
                readConversations = readConversations,
            ),
            eventHandler = ChatPreviewsEventHandler.Blank,
        )
    }
}

@Preview
@Composable
private fun NoUnreadMessagesPreview() {
    NeonTheme {
        ChatPreviews(
            state = LoadedChatPreviewsState(
                nudgedConversations = emptyList(),
                unreadConversations = emptyList(),
                readConversations = readConversations,
            ),
            eventHandler = ChatPreviewsEventHandler.Blank,
        )
    }
}

@Preview
@Composable
private fun NoReadMessagesPreview() {
    NeonTheme {
        ChatPreviews(
            state = LoadedChatPreviewsState(
                nudgedConversations = emptyList(),
                unreadConversations = unreadConversations,
                readConversations = emptyList(),
            ),
            eventHandler = ChatPreviewsEventHandler.Blank,
        )
    }
}